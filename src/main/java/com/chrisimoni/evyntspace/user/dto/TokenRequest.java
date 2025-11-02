package com.chrisimoni.evyntspace.user.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank(message = "Token cannot be empty")
        String token
) {
}
