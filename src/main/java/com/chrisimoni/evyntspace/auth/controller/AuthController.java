package com.chrisimoni.evyntspace.auth.controller;

import com.chrisimoni.evyntspace.auth.dto.VerificationConfirmRequest;
import com.chrisimoni.evyntspace.auth.dto.VerificationRequest;
import com.chrisimoni.evyntspace.auth.dto.VerificationResponse;
import com.chrisimoni.evyntspace.auth.model.VerifiedSession;
import com.chrisimoni.evyntspace.auth.service.VerificationService;
import com.chrisimoni.evyntspace.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    //Todo: create authservice
    //private final AuthService authService
    private final VerificationService verificationService;

    @PostMapping("/request-verification-code")
    public ApiResponse<Void> requestVerificationCode(
            @Valid @RequestBody VerificationRequest request) {
        verificationService.requestVerificationCode(request.email());
        return ApiResponse.success("Verification code sent. Please check your email.");
    }

    @PostMapping("/verify-code")
    public ApiResponse<VerificationResponse> verifyCode(
            @Valid @RequestBody VerificationConfirmRequest request) {
        VerifiedSession session = verificationService.confirmVerificationCode(
                request.email(), request.code());
        VerificationResponse response = new VerificationResponse(session.getId(), session.getExpirationTimeInMinutes());
        return ApiResponse.success("Email successful verified.", response);
    }

    //TODO: login

    //TODO: refresh-token

    //TODO: reset-password

    //TODO: change-password
}
