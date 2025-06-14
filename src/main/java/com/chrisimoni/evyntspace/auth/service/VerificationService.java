package com.chrisimoni.evyntspace.auth.service;

import com.chrisimoni.evyntspace.auth.model.VerifiedSession;

public interface VerificationService {
    void requestVerificationCode(String email);
    VerifiedSession confirmVerificationCode(String email, String code);
}
