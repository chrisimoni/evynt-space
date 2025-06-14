package com.chrisimoni.evyntspace.user.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String company,
        String phoneNumber,
        boolean active,
        String profileImgUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
