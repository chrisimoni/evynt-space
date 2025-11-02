package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.user.model.Token;
import com.chrisimoni.evyntspace.user.model.User;
import jakarta.validation.constraints.NotBlank;

public interface TokenService {
    String createRefreshToken(User user, int validityInMinutes);
    Token verifyToken(String token);
    String createPasswordResetToken(User user, int validityInMinutes);
}
