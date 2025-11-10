package com.chrisimoni.evyntspace.user.service.impl;

import com.chrisimoni.evyntspace.common.exception.InvalidTokenException;
import com.chrisimoni.evyntspace.user.enums.TokenType;
import com.chrisimoni.evyntspace.user.model.Token;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.repository.TokenRepository;
import com.chrisimoni.evyntspace.user.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.chrisimoni.evyntspace.user.util.UserUtil.generateToken;
import static com.chrisimoni.evyntspace.user.util.UserUtil.hash;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final TokenRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String createRefreshToken(User user, int validityInMinutes) {
        // Delete old refresh tokens for this user
        repository.deleteByUserAndTokenType(user, TokenType.REFRESH_TOKEN);
        return createToken(user, TokenType.REFRESH_TOKEN, validityInMinutes);
    }

    @Override
    @Transactional
    public String createPasswordResetToken(User user, int validityInMinutes) {
        // Delete old newPassword reset tokens for this user
        repository.deleteByUserAndTokenType(user, TokenType.PASSWORD_RESET_TOKEN);
        return createToken(user, TokenType.PASSWORD_RESET_TOKEN, validityInMinutes);
    }

    @Override
    @Transactional
    public String createLoginToken(User user, int validityInMinutes) {
        repository.deleteByUserAndTokenType(user, TokenType.LOGIN_TOKEN);
        return createToken(user, TokenType.LOGIN_TOKEN, validityInMinutes);
    }

    @Transactional
    public void deleteToken(Token token) {
        repository.delete(token);
    }

    @Transactional
    public void deleteToken(String plainToken) {
        repository.delete(findByToken(plainToken));
    }

    @Override
    @Transactional
    public Token verifyToken(String plainToken) {
        Token token = findByToken(plainToken);

        if (token.isExpired()) {
            // Clean up expired token
            repository.delete(token);
            throw new InvalidTokenException(
                    String.format("%s has expired", token.getTokenType())
            );
        }

        return token;
    }

    private String createToken(User user, TokenType tokenType, int validityInMinutes) {
        String plainToken = TokenType.LOGIN_TOKEN.equals(tokenType) ? generateCode() : generateToken();
        String hashedToken = hash(plainToken);

        Instant expiry = Instant.now().plus(validityInMinutes, ChronoUnit.MINUTES);

        Token token = Token.builder()
                .user(user)
                .token(hashedToken)
                .tokenType(tokenType)
                .expiryDate(expiry)
                .build();

        repository.save(token);

        return plainToken;
    }

    //TODO: refactor later
    private String generateCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    private Token findByToken(String plainToken) {
        String hashedToken = hash(plainToken);
        return repository.findByToken(hashedToken)
                .orElseThrow(() -> new InvalidTokenException(
                        "Invalid or expired token"
                ));
    }
}
