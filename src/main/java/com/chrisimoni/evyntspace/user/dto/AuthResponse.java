package com.chrisimoni.evyntspace.user.dto;

import java.util.UUID;

public record AuthResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String accessToken,
        String refreshToken
) {
}
