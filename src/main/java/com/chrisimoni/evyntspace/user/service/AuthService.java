package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.user.dto.AuthRequest;
import com.chrisimoni.evyntspace.user.dto.AuthResponse;
import com.chrisimoni.evyntspace.user.dto.ChangePasswordRequest;
import com.chrisimoni.evyntspace.user.dto.UserCreateRequest;
import com.chrisimoni.evyntspace.user.model.VerifiedSession;

import java.util.UUID;

public interface AuthService {
    void requestVerificationCode(String email);
    VerifiedSession confirmVerificationCode(String email, String code);
    void verifyEmailSession(String email, UUID verficationToken);
    AuthResponse signup(UserCreateRequest request);
    AuthResponse login(AuthRequest request);
    AuthResponse refreshToken(String refreshTokenString);
    void requestPasswordReset(String email);
    void resetPassword(String tokenString, String newPassword);
    void logout(String token);
    void changePassword(UUID userId, ChangePasswordRequest request);
    void requestLoginCode(String email);
    AuthResponse verifyAndGenerateToken(String email, String code);
}
