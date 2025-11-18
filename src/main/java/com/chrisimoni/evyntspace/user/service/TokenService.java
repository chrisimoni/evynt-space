package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.user.model.Token;
import com.chrisimoni.evyntspace.user.model.User;
import jakarta.validation.constraints.NotBlank;

public interface TokenService {
    String createRefreshToken(User user);
    Token verifyToken(String token);
    void createPasswordResetToken(User user);
    void deleteToken(Token token);
    void deleteToken(String plainToken);
    String createLoginToken(User user, int codeValidity);
}
