package com.chrisimoni.evyntspace.user.service.impl;

import com.chrisimoni.evyntspace.common.exception.InvalidTokenException;
import com.chrisimoni.evyntspace.user.enums.TokenType;
import com.chrisimoni.evyntspace.user.model.Token;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.repository.TokenRepository;
import com.chrisimoni.evyntspace.user.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public String createRefreshToken(User user, int validityInMinutes) {
        return createToken(user, TokenType.REFRESH_TOKEN, validityInMinutes);
    }

    private String createToken(User user, TokenType tokenType, int validityInMinutes) {
        String plainToken = generateToken();
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

    @Override
    public Token verifyToken(String token) {
        Token tokenObj = findByToken(token);

        if (tokenObj.isExpired()) {
            repository.delete(tokenObj);
            throw new InvalidTokenException(
                    String.format("%s has expired.", tokenObj.getTokenType()));
        }

        if (tokenObj.isRevoked()) {
            throw new InvalidTokenException(
                    String.format("%s was revoked. Please log in again", tokenObj.getTokenType())
            );
        }

        return tokenObj;
    }

    @Override
    public String createPasswordResetToken(User user, int validityInMinutes) {
        return createToken(user, TokenType.PASSWORD_RESET_TOKEN, validityInMinutes);
    }

    public void revokeToken(String token) {
        Token tokenObj = findByToken(token);
        tokenObj.setRevoked(true);
        repository.save(tokenObj);
    }

    private Token findByToken(String token) {
        String hashedToken = hash(token);;
        return repository.findByToken(hashedToken)
                .orElseThrow(() -> new InvalidTokenException("The provided token is not found."));
    }
}
