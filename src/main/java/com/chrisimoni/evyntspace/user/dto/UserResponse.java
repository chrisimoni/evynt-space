package com.chrisimoni.evyntspace.user.dto;

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
        Instant deactivatedAt,
        String profileImageUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
