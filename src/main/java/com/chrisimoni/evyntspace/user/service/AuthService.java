package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.model.VerifiedSession;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface AuthService {
    void requestVerificationCode(String email);
    VerifiedSession confirmVerificationCode(String email, String code);
    void verifyEmailSession(String email, UUID verficationToken);
    User signup(User model, UUID verficationToken);
}
