package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.user.event.VerificationCodeRequestedEvent;
import com.chrisimoni.evyntspace.user.model.VerificationCode;
import com.chrisimoni.evyntspace.user.model.VerifiedSession;
import com.chrisimoni.evyntspace.user.repository.VerificationCodeRepository;
import com.chrisimoni.evyntspace.user.repository.VerificationSessionRepository;
import com.chrisimoni.evyntspace.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

import static com.chrisimoni.evyntspace.common.util.ValidationUtil.validateEmailFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationServiceImpl implements VerificationService{
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationSessionRepository sessionRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    private static final int CODE_VALIDITY_MINUTES = 5;
    private static final int SESSION_VALIDITY_MINUTES = 15; // How long the verified token is valid for user creation

    @Override
    @Transactional
    public void requestVerificationCode(String email) {
        validateEmailFormat(email);
        userService.validateEmailIsUnique(email);
        verificationCodeRepository.invalidatePreviousCodes(email);

        String code = generateAndSaveCode(email);

        eventPublisher.publishEvent(new VerificationCodeRequestedEvent(
                this, email, code, CODE_VALIDITY_MINUTES));
    }

    @Override
    @Transactional
    public VerifiedSession confirmVerificationCode(String email, String code) {
        validateEmailFormat(email);
        VerificationCode latestCode = verificationCodeRepository
                .findActiveVerificatonCodeByEmail(email, Instant.now())
                .orElseThrow(() -> new BadRequestException("Verification failed: No active code found or code expired/used."));

        //TODO: verify the hashed codes
        if(!Objects.equals(code, latestCode.getCode())) {
            throw new BadRequestException("Verification failed: Invalid code.");
        }

        latestCode.setUsed(true);
        verificationCodeRepository.save(latestCode);

        return createSession(email);
    }

    @Transactional
    protected VerifiedSession createSession(String email) {
        sessionRepository.invalidatePreviousSessions(email);
        Instant sessionExpirationTime = Instant.now().plus(SESSION_VALIDITY_MINUTES, ChronoUnit.MINUTES);
        VerifiedSession session = new VerifiedSession(email, sessionExpirationTime);
        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public void verifyEmailSession(String email, UUID verficationToken) {
        //validate email verification token
        VerifiedSession verifiedSession = sessionRepository
                .findByIdAndIsUsedFalseAndExpirationTimeAfter(verficationToken, Instant.now())
                .orElseThrow(() -> new BadRequestException(
                        "Email verification token is invalid or expired. Please re-verify your email."));

        if(!verifiedSession.getEmail().equalsIgnoreCase(email)) {
            throw new BadRequestException("Email in request does not match verified email in token.");
        }

        //mark the verification session as used to prevent reuse
        verifiedSession.setUsed(true);
        sessionRepository.save(verifiedSession);
    }

    String generateAndSaveCode(String email) {
        //generate 6-digit code
        //TODO: hash the generated code with passwordEncoder before saving to db
        String plainCode = String.valueOf((int)(Math.random() * 900000) + 100000);
        Instant expirationTime = Instant.now().plus(CODE_VALIDITY_MINUTES, ChronoUnit.MINUTES);
        VerificationCode verificationCode = new VerificationCode(email, plainCode, expirationTime);
        verificationCodeRepository.save(verificationCode);

        return plainCode;
    }
}
