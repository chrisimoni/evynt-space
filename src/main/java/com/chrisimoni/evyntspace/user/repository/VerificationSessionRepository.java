package com.chrisimoni.evyntspace.user.repository;

import com.chrisimoni.evyntspace.user.model.VerifiedSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationSessionRepository extends JpaRepository<VerifiedSession, UUID> {
    //Find an active, unused verification session
    Optional<VerifiedSession> findByIdAndIsUsedFalseAndExpirationTimeAfter(UUID id, Instant now);

    @Modifying
    @Query(value = "UPDATE verified_sessions SET is_used = true WHERE email = :email AND is_used = false",
            nativeQuery = true)
    void invalidatePreviousSessions(@Param("email") String email);
}
