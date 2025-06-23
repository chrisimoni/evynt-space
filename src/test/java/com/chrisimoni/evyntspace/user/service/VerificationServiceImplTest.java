package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.common.exception.BadRequestException;
import com.chrisimoni.evyntspace.common.exception.DuplicateResourceException;
import com.chrisimoni.evyntspace.user.event.VerificationCodeRequestedEvent;
import com.chrisimoni.evyntspace.user.model.VerificationCode;
import com.chrisimoni.evyntspace.user.model.VerifiedSession;
import com.chrisimoni.evyntspace.user.repository.VerificationCodeRepository;
import com.chrisimoni.evyntspace.user.repository.VerificationSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VerificationServiceImplTest {
    @Mock
    private UserService userService;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private VerificationSessionRepository sessionRepository;

    @InjectMocks
    private VerificationServiceImpl verificationService;

    @Test
    @DisplayName("Should successfully request verification code and publish event")
    void testRequestVerificationCode_shouldPass() {
        // Arrange
        String email = "test@example.com";
        String generatedCode = "123456";
        int validityMinutes = 5;

        // Mock behavior of dependencies
        // void methods: doNothing() is default for void, but explicit for clarity
        doNothing().when(userService).validateEmailIsUnique(email);
        doNothing().when(verificationCodeRepository).invalidatePreviousCodes(email);

        // Mock the private generateAndSaveCode method
        // We use Mockito.spy to partially mock the actual service instance
        // Then we define behavior for its private method.
        VerificationServiceImpl spyVerificationService = Mockito.spy(verificationService);
        doReturn(generatedCode).when(spyVerificationService).generateAndSaveCode(email);

        // Call the method under test
        spyVerificationService.requestVerificationCode(email);

        // Verify interactions
        verify(userService, times(1)).validateEmailIsUnique(email);
        verify(verificationCodeRepository, times(1)).invalidatePreviousCodes(email);
        verify(spyVerificationService, times(1)).generateAndSaveCode(email);

        // Verify the event was published with correct details
        ArgumentCaptor<VerificationCodeRequestedEvent> eventCaptor = ArgumentCaptor.forClass(
                VerificationCodeRequestedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        VerificationCodeRequestedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getRecipient()).isEqualTo(email);
        assertThat(capturedEvent.getVerificationCode()).isEqualTo(generatedCode);
        assertThat(capturedEvent.getCodeValidityInMinutes()).isEqualTo(validityMinutes);
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid email format")
    void testRequestVerificationCode_InvalidEmailFormat_ThrowsException() {
        String invalidEmail = "invalid-email";

        assertThrows(BadRequestException.class, () -> { // Assert it throws YOUR custom exception
            verificationService.requestVerificationCode(invalidEmail);
        });

        // Optional: Verify no other interactions happened if validation failed early
        verifyNoInteractions(userService);
        verifyNoInteractions(verificationCodeRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should throw exception if email is not unique")
    void testRequestVerificationCode_EmailNotUnique_ThrowsException() {
        String existingEmail = "existing@example.com";

        doThrow(new DuplicateResourceException("This email is already registered. " +
                "Please login or reset your password."))
                .when(userService).validateEmailIsUnique(existingEmail);

        // Verify that calling the method throws the expected exception
        assertThrows(DuplicateResourceException.class, () -> {
            verificationService.requestVerificationCode(existingEmail);
        });

        // Verify interaction up to the point of failure
        verify(userService, times(1)).validateEmailIsUnique(existingEmail);
        verifyNoMoreInteractions(verificationCodeRepository, eventPublisher);
    }

    @Test
    @DisplayName("Should confirm verification code and create a session")
    void testConfirmVerificationCode_shouldPass() {
        // Arrange
        String email = "test@example.com";
        String inputCode = "123456";
        int sessionValidityInMinutes = 15;

        Instant now = Instant.now();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode(inputCode);
        verificationCode.setUsed(false);

//        when(verificationCodeRepository.findActiveVerificatonCodeByEmail(email, now))
//                .thenReturn(Optional.of(verificationCode));
        when(verificationCodeRepository.findActiveVerificatonCodeByEmail(eq(email), any(Instant.class)))
                .thenReturn(Optional.of(verificationCode));

        VerifiedSession expectedSession = new VerifiedSession(
                email,
                Instant.now().plus(sessionValidityInMinutes, ChronoUnit.MINUTES));
        when(sessionRepository.save(any(VerifiedSession.class))).thenReturn(expectedSession);

        // Act
        VerifiedSession session = verificationService.confirmVerificationCode(email, inputCode);

        // Assert
        assertNotNull(session, "Session should not be null");
        assertEquals(email, session.getEmail(), "Session email should match");
        assertNotNull(session.getExpirationTime(), "Session expiration time should not be null");

        verify(verificationCodeRepository, times(1))
                .findActiveVerificatonCodeByEmail(eq(email), any(Instant.class));

        verify(verificationCodeRepository, times(1)).save(verificationCode);
        verify(sessionRepository, times(1)).invalidatePreviousSessions(email);
        verify(sessionRepository, times(1)).save(any(VerifiedSession.class));
    }

    @Test
    @DisplayName("confirmVerificationCode: Should throw BadRequestException if no active code found")
    void testConfirmVerificationCode_NoActiveCodeFound_ThrowsException() {
        String email = "test@example.com";
        String someCode = "123456";

        // Mock repository to return empty Optional
        when(verificationCodeRepository.findActiveVerificatonCodeByEmail(eq(email), any(Instant.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            verificationService.confirmVerificationCode(email, someCode);
        });

        assertEquals("Verification failed: No active code found or code expired/used.", thrown.getMessage());
    }

    @Test
    @DisplayName("confirmVerificationCode: Should throw BadRequestException if code mismatch")
    void testConfirmVerificationCode_CodeMismatch_ThrowsException() {
        String email = "test@example.com";
        String inputCode = "WRONGCODE";
        String correctCode = "123456";

        VerificationCode activeCode = new VerificationCode(
                email, correctCode, Instant.now().plus(10, ChronoUnit.MINUTES));

        when(verificationCodeRepository.findActiveVerificatonCodeByEmail(eq(email), any(Instant.class)))
                .thenReturn(Optional.of(activeCode));

        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            verificationService.confirmVerificationCode(email, inputCode);
        });

        assertEquals("Verification failed: Invalid code.", thrown.getMessage());
    }

}
