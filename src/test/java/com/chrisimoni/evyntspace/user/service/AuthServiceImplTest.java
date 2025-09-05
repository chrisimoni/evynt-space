package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.common.exception.BadRequestException;
import com.chrisimoni.evyntspace.common.exception.DuplicateResourceException;
import com.chrisimoni.evyntspace.user.events.VerificationCodeRequestedEvent;
import com.chrisimoni.evyntspace.user.model.VerificationCode;
import com.chrisimoni.evyntspace.user.model.VerifiedSession;
import com.chrisimoni.evyntspace.user.repository.VerificationCodeRepository;
import com.chrisimoni.evyntspace.user.repository.VerificationSessionRepository;
import com.chrisimoni.evyntspace.user.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Mock
    private UserService userService;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private VerificationSessionRepository sessionRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private final UUID TEST_TOKEN = UUID.randomUUID();
    private final String TEST_EMAIL = "test@example.com";

    @Test
    @DisplayName("Should successfully request verification code and publish event")
    void testRequestVerificationCode_shouldPass() {
        // Arrange
        String generatedCode = "123456";
        int validityMinutes = 5;

        // Mock behavior of dependencies
        // void methods: doNothing() is default for void, but explicit for clarity
        doNothing().when(userService).validateEmailIsUnique(TEST_EMAIL);
        doNothing().when(verificationCodeRepository).invalidatePreviousCodes(TEST_EMAIL);

        // Mock the private generateAndSaveCode method
        // We use Mockito.spy to partially mock the actual service instance
        // Then we define behavior for its private method.
        AuthServiceImpl spyVerificationService = Mockito.spy(authService);
        doReturn(generatedCode).when(spyVerificationService).generateAndSaveCode(TEST_EMAIL);

        // Call the method under test
        spyVerificationService.requestVerificationCode(TEST_EMAIL);

        // Verify interactions
        verify(userService, times(1)).validateEmailIsUnique(TEST_EMAIL);
        verify(verificationCodeRepository, times(1)).invalidatePreviousCodes(TEST_EMAIL);
        verify(spyVerificationService, times(1)).generateAndSaveCode(TEST_EMAIL);

        // Verify the event was published with correct details
        ArgumentCaptor<VerificationCodeRequestedEvent> eventCaptor = ArgumentCaptor.forClass(
                VerificationCodeRequestedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        VerificationCodeRequestedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getRecipient()).isEqualTo(TEST_EMAIL);
        assertThat(capturedEvent.getVerificationCode()).isEqualTo(generatedCode);
        assertThat(capturedEvent.getCodeValidityInMinutes()).isEqualTo(validityMinutes);
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid email format")
    void testRequestVerificationCode_InvalidEmailFormat_ThrowsException() {
        String invalidEmail = "invalid-email";

        assertThrows(BadRequestException.class, () -> { // Assert it throws YOUR custom exception
            authService.requestVerificationCode(invalidEmail);
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
            authService.requestVerificationCode(existingEmail);
        });

        // Verify interaction up to the point of failure
        verify(userService, times(1)).validateEmailIsUnique(existingEmail);
        verifyNoMoreInteractions(verificationCodeRepository, eventPublisher);
    }

    @Test
    @DisplayName("Should confirm verification code and create a session")
    void testConfirmVerificationCode_shouldPass() {
        String inputCode = "123456";
        int sessionValidityInMinutes = 15;

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode(inputCode);
        verificationCode.setUsed(false);

        when(verificationCodeRepository.findActiveVerificatonCodeByEmail(eq(TEST_EMAIL), any(Instant.class)))
                .thenReturn(Optional.of(verificationCode));

        VerifiedSession expectedSession = new VerifiedSession(
                TEST_EMAIL,
                Instant.now().plus(sessionValidityInMinutes, ChronoUnit.MINUTES));
        when(sessionRepository.save(any(VerifiedSession.class))).thenReturn(expectedSession);

        // Act
        VerifiedSession session = authService.confirmVerificationCode(TEST_EMAIL, inputCode);

        // Assert
        assertNotNull(session, "Session should not be null");
        assertEquals(TEST_EMAIL, session.getEmail(), "Session email should match");
        assertNotNull(session.getExpirationTime(), "Session expiration time should not be null");

        verify(verificationCodeRepository, times(1))
                .findActiveVerificatonCodeByEmail(eq(TEST_EMAIL), any(Instant.class));

        verify(verificationCodeRepository, times(1)).save(verificationCode);
        verify(sessionRepository, times(1)).invalidatePreviousSessions(TEST_EMAIL);
        verify(sessionRepository, times(1)).save(any(VerifiedSession.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException if no active code found")
    void testConfirmVerificationCode_NoActiveCodeFound_ThrowsException() {
        String someCode = "123456";

        // Mock repository to return empty Optional
        when(verificationCodeRepository.findActiveVerificatonCodeByEmail(eq(TEST_EMAIL), any(Instant.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            authService.confirmVerificationCode(TEST_EMAIL, someCode);
        });

        assertEquals("Verification failed: No active code found or code expired/used.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException if code mismatch")
    void testConfirmVerificationCode_CodeMismatch_ThrowsException() {
        String inputCode = "WRONGCODE";
        String correctCode = "123456";

        VerificationCode activeCode = new VerificationCode(
                TEST_EMAIL, correctCode, Instant.now().plus(10, ChronoUnit.MINUTES));

        when(verificationCodeRepository.findActiveVerificatonCodeByEmail(eq(TEST_EMAIL), any(Instant.class)))
                .thenReturn(Optional.of(activeCode));

        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            authService.confirmVerificationCode(TEST_EMAIL, inputCode);
        });

        assertEquals("Verification failed: Invalid code.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should verify email session")
    void testVerifyEmailSession_shouldPass() {
        VerifiedSession session = mockSession();

        when(sessionRepository.findByIdAndIsUsedFalseAndExpirationTimeAfter(eq(TEST_TOKEN), any()))
                .thenReturn(Optional.of(session));

        authService.verifyEmailSession(TEST_EMAIL, TEST_TOKEN);

        assertTrue(session.isUsed());
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Should throw BadRequestException when token is invalid/expired")
    void testVerifyEmailSession_WhenTokenIsInvalidOrExpired_ThrowsException() {

        when(sessionRepository.findByIdAndIsUsedFalseAndExpirationTimeAfter(eq(TEST_TOKEN), any()))
                .thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                authService.verifyEmailSession(TEST_EMAIL, TEST_TOKEN));

        assertEquals("Email verification token is invalid or expired. Please re-verify your email.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when email does not match email in session")
    void  testVerifyEmailSession_WhenEmailDoesNotMatch_ThrowsException() {
        VerifiedSession session = mockSession();
        session.setEmail("other@example.com");

        when(sessionRepository.findByIdAndIsUsedFalseAndExpirationTimeAfter(eq(TEST_TOKEN), any()))
                .thenReturn(Optional.of(session));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                authService.verifyEmailSession(TEST_EMAIL, TEST_TOKEN));

        assertEquals("Email in request does not match verified email in token.", ex.getMessage());
    }

    private VerifiedSession mockSession() {
        Instant expirationTime = Instant.now().plus(Duration.ofMinutes(15));
        VerifiedSession session = new VerifiedSession(TEST_EMAIL, expirationTime);
        session.setId(TEST_TOKEN);
        return session;
    }
}
