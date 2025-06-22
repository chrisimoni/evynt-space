package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.user.model.VerifiedSession;

public interface VerificationService {
    void requestVerificationCode(String email);
    VerifiedSession confirmVerificationCode(String email, String code);
}
