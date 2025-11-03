package com.chrisimoni.evyntspace.user.scheduler;

import com.chrisimoni.evyntspace.user.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserScheduler {
    private final TokenRepository tokenRepository;

    // Scheduled cleanup of expired tokens
    @Scheduled(cron = "0 0 3 * * ?")  // Run at 3 AM daily
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = tokenRepository.deleteByExpiryDateBefore(Instant.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired tokens", deleted);
        }
    }
}
