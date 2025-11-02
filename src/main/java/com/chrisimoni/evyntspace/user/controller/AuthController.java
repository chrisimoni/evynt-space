package com.chrisimoni.evyntspace.user.controller;

import com.chrisimoni.evyntspace.user.dto.*;
import com.chrisimoni.evyntspace.user.mapper.UserMapper;
import com.chrisimoni.evyntspace.user.model.VerifiedSession;
import com.chrisimoni.evyntspace.user.service.UserService;
import com.chrisimoni.evyntspace.user.service.AuthService;
import com.chrisimoni.evyntspace.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    //Todo: create authservice
    //private final AuthService authService
    private final AuthService authService;
    private final UserService userService;
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
        AuthResponse response = authService.refreshToken(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<AuthResponse> forgotPassword(@Valid @RequestBody EmailRequest request) {
        authService.resetPasswordToken(request);
        return ApiResponse.success("Please check your email for password reset link.");
    }

    //TODO: reset password

    //TODO: change-password
}
