package com.chrisimoni.evyntspace.user.controller;

import com.chrisimoni.evyntspace.common.exception.BadRequestException;
import com.chrisimoni.evyntspace.user.dto.UserCreateRequest;
import com.chrisimoni.evyntspace.user.dto.VerificationConfirmRequest;
import com.chrisimoni.evyntspace.user.dto.VerificationRequest;
import com.chrisimoni.evyntspace.user.mapper.UserMapper;
import com.chrisimoni.evyntspace.user.model.VerifiedSession;
import com.chrisimoni.evyntspace.user.service.UserService;
import com.chrisimoni.evyntspace.user.service.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest focuses on Spring MVC components, auto-configures MockMvc,
// and only scans controller-related beans.
@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc; // For performing HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to/from JSON

    // Mock beans for all dependencies of AuthController's constructor
    @MockBean
    private VerificationService verificationService;
    @MockBean
    private UserService userService; // Mocked because AuthController injects it
    @MockBean
    private UserMapper userMapper;   // Mocked because AuthController injects it

    private static final String BASE_AUTH_URL = "/api/v1/auth";

    @Test
    @DisplayName("Should successfully request verification code")
    void testRequestVerificationCode_shouldPass() throws Exception {
        // Arrange
        VerificationRequest request = new VerificationRequest("test@example.com");
        String requestJson = new ObjectMapper().writeValueAsString(request);

        // Define mock behavior for the service call
        doNothing().when(verificationService).requestVerificationCode(request.email());

        // Act & Assert
        mockMvc.perform(post(BASE_AUTH_URL + "/request-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message")
                        .value("Verification code sent. Please check your email."));

        // Optional: Verify interaction with the service
        verify(verificationService, times(1)).requestVerificationCode(request.email());
    }

    @Test
    @DisplayName("Should return 400 when request is invalid")
    void testRequestVerificationCode_shouldReturnBadRequestOnInvalidInput() throws Exception {
        // Arrange
        VerificationRequest request = new VerificationRequest("invalid-email");
        String requestJson = new ObjectMapper().writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post(BASE_AUTH_URL + "/request-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors").exists()); // Assuming validation errors are returned

        //Optional: Verify no interaction with the service
        verify(verificationService, never()).requestVerificationCode(anyString());
    }

    @Test
    @DisplayName("Should successfully verify code and return session details")
    void testVerifyCode_shouldPass() throws Exception {
        // Arrange
        String testEmail = "verify@example.com";
        String testCode = "123456";
        VerificationConfirmRequest request = new VerificationConfirmRequest(testEmail, testCode);
        String requestJson = objectMapper.writeValueAsString(request);

        UUID sessionId = UUID.randomUUID();
        Instant expirationTime = Instant.now().plusSeconds(900); // 15 mins from now

        // Create a mock VerifiedSession that will be returned by the service
        VerifiedSession mockSession = new VerifiedSession();
        mockSession.setId(sessionId);
        mockSession.setEmail(testEmail);
        mockSession.setExpirationTime(expirationTime);

        // Define mock behavior for verificationService.confirmVerificationCode
        doReturn(mockSession).when(verificationService)
                .confirmVerificationCode(eq(testEmail), eq(testCode));

        // Act & Assert
        mockMvc.perform(post(BASE_AUTH_URL + "/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Email successfully verified."))
                .andExpect(jsonPath("$.data.verificationToken").value(sessionId.toString()));


        //Optional: Verify service method was called once with correct arguments
        verify(verificationService, times(1))
                .confirmVerificationCode(eq(testEmail), eq(testCode));
    }

    @Test
    @DisplayName("Should return 400 for invalid code")
    void testVerifyCode_InvalidCode_ThrowsBadRequest() throws Exception {
        // Arrange
        String testEmail = "test@example.com";
        String testCode = "wrongcode";
        VerificationConfirmRequest request = new VerificationConfirmRequest(testEmail, testCode);
        String requestJson = objectMapper.writeValueAsString(request);

        String errorMessage = "Verification failed: Invalid code.";

        // Mock service to throw BadRequestException
        doThrow(new BadRequestException(errorMessage)).when(verificationService)
                .confirmVerificationCode(eq(testEmail), eq(testCode));

        // Act & Assert
        mockMvc.perform(post(BASE_AUTH_URL + "/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest()) // Expect HTTP 400 Bad Request
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(errorMessage));

        // Verify service method was called once
        verify(verificationService, times(1)).confirmVerificationCode(eq(testEmail), eq(testCode));
    }

    @Test
    @DisplayName("Should successfully create a user")
    void testCreateUser_Success() throws Exception {
        // Arrange
        UserCreateRequest request = createUserRequestObject(UUID.randomUUID());
        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post(BASE_AUTH_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated()) // Expect HTTP 201 Created
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("User created."));
    }

    @Test
    @DisplayName("POST /auth/signup - Should return 400 for null verificationToken")
    void createUser_NullVerificationToken_BadRequest() throws Exception {
        // Arrange
        UserCreateRequest request = createUserRequestObject(null);
        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(post(BASE_AUTH_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").exists());
    }

    private UserCreateRequest createUserRequestObject(UUID verificationToken) {
        return new UserCreateRequest(
                verificationToken,
                "newuser@example.com",
                "John",
                "Doe",
                "StrongPassword123!",
                "TestCompany",
                "1234567890");
    }
}
