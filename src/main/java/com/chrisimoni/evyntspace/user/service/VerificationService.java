package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.user.model.VerifiedSession;

import java.util.UUID;

public interface VerificationService {
    void requestVerificationCode(String email);
    VerifiedSession confirmVerificationCode(String email, String code);
    void verifyEmailSession(String email, UUID verficationToken);
}
