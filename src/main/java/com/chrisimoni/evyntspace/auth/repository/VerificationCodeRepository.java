package com.chrisimoni.evyntspace.auth.repository;

import com.chrisimoni.evyntspace.auth.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    @Modifying
    @Query(value = "UPDATE verification_codes SET is_used = true WHERE email = :email AND is_used = false",
            nativeQuery = true)
    void invalidatePreviousCodes(@Param("email") String email);

    @Query(value = """
            SELECT *
            FROM verification_codes
            WHERE email = :email
              AND is_used = false
              AND expiration_time > :now
            ORDER BY created_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<VerificationCode> findActiveVerificatonCodeByEmail(
            @Param("email") String email,
            @Param("now") LocalDateTime now);
}
