package com.chrisimoni.evyntspace.user.controller;

import com.chrisimoni.evyntspace.user.dto.*;
import com.chrisimoni.evyntspace.user.mapper.UserMapper;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.model.VerifiedSession;
import com.chrisimoni.evyntspace.user.service.AuthService;
import com.chrisimoni.evyntspace.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/request-verification-code")
    public ApiResponse<Void> requestVerificationCode(
            @Valid @RequestBody EmailRequest request) {
        authService.requestVerificationCode(request.email());
        return ApiResponse.success("Verification code sent. Please check your email.");
    }

    @PostMapping("/verify-code")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VerificationResponse> verifyCode(
            @Valid @RequestBody VerificationConfirmRequest request) {
        VerifiedSession session = authService.confirmVerificationCode(
                request.email(), request.code());
        VerificationResponse response = new VerificationResponse(session.getId(), session.getExpirationTime());
        return ApiResponse.success("Email successfully verified.", response);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody UserCreateRequest request) {
        AuthResponse response = authService.signup(userMapper.toModel(request), request.verificationToken());
        return ApiResponse.success("User created.", response);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody TokenRequest request) {
        AuthResponse response = authService.refreshToken(request.token());
        return ApiResponse.success(response);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody EmailRequest request) {
        authService.requestPasswordReset(request.email());
        return ApiResponse.success("Please check your email for newPassword reset link.");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ApiResponse.success("Password successfully reset.");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody TokenRequest request) {
        authService.logout(request.token());
        return ApiResponse.success("Logged out successfully.");
    }

    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                 Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();
        authService.changePassword(userId, request);
        return ApiResponse.success("Password updated successfully.");
    }
}
