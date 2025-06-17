package com.chrisimoni.evyntspace.auth.service;

import com.chrisimoni.evyntspace.auth.event.VerificationCodeRequestedEvent;
import com.chrisimoni.evyntspace.auth.model.VerificationCode;
import com.chrisimoni.evyntspace.auth.model.VerifiedSession;
import com.chrisimoni.evyntspace.auth.repository.VerificationCodeRepository;
import com.chrisimoni.evyntspace.auth.repository.VerificationSessionRepository;
import com.chrisimoni.evyntspace.common.exception.BadRequestException;
import com.chrisimoni.evyntspace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

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

        String code = generateVerificationCode(email);

        eventPublisher.publishEvent(new VerificationCodeRequestedEvent(
                this, email, code, CODE_VALIDITY_MINUTES));
    }

    @Override
    @Transactional
    public VerifiedSession confirmVerificationCode(String email, String code) {
        validateEmailFormat(email);
        VerificationCode latestCode = verificationCodeRepository
                .findActiveVerificatonCodeByEmail(email, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Verification failed: No active code found or code expired/used."));

        //TODO: verify the hashed codes
        if(!Objects.equals(code, latestCode.getCode())) {
            throw new BadRequestException("Verification failed: Invalid code.");
        }

        latestCode.setUsed(true);
        verificationCodeRepository.save(latestCode);

        return createSession(email);
    }

    private VerifiedSession createSession(String email) {
        LocalDateTime sessionExpirationTime = LocalDateTime.now().plusMinutes(SESSION_VALIDITY_MINUTES);
        VerifiedSession session = new VerifiedSession(email, SESSION_VALIDITY_MINUTES, sessionExpirationTime);
        return sessionRepository.save(session);
    }

    private String generateVerificationCode(String email) {
        //generate 6-digit code
        //TODO: hash the generated code with passwordEncoder before saving to db
        String plainCode = String.valueOf((int)(Math.random() * 900000) + 100000);
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES);
        Instant expirationInstant = expirationTime.atZone(ZoneId.of("UTC")).toInstant();
        VerificationCode verificationCode = new VerificationCode(email, plainCode, expirationInstant);
        verificationCodeRepository.save(verificationCode);

        return plainCode;
    }
}
