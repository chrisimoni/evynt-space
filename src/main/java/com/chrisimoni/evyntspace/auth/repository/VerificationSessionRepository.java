package com.chrisimoni.evyntspace.auth.repository;

import com.chrisimoni.evyntspace.auth.model.VerifiedSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface VerificationSessionRepository extends JpaRepository<VerifiedSession, UUID> {
    //Find an active, unused verification session
    Optional<VerifiedSession> findByIdAndIsUsedFalseAndExpirationTimeAfter(UUID id, LocalDateTime now);
}
