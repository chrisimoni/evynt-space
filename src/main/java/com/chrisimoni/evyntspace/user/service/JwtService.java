package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.user.model.User;

public interface JwtService {
    String generateToken(User user);
    String extractUsername(String token);
    boolean isTokenValid(String token);
}
