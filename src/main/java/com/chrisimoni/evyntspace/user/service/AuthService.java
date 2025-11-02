package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.user.dto.AuthRequest;
import com.chrisimoni.evyntspace.user.dto.AuthResponse;
import com.chrisimoni.evyntspace.user.dto.EmailRequest;
import com.chrisimoni.evyntspace.user.dto.TokenRequest;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.model.VerifiedSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface AuthService {
    void requestVerificationCode(String email);
    VerifiedSession confirmVerificationCode(String email, String code);
    void verifyEmailSession(String email, UUID verficationToken);
    AuthResponse signup(User model, UUID verficationToken);
    AuthResponse login(AuthRequest request);
    AuthResponse refreshToken(TokenRequest request);
    void resetPasswordToken(@Valid EmailRequest request);
}
