package com.chrisimoni.evyntspace.user.repository;

import com.chrisimoni.evyntspace.user.enums.TokenType;
import com.chrisimoni.evyntspace.user.model.Token;
import com.chrisimoni.evyntspace.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByToken(String token);
    void deleteByUserAndTokenType(User user, TokenType tokenType);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.expiryDate < :date")
    int deleteByExpiryDateBefore(Instant date);
}
