package com.chrisimoni.evyntspace.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email must be a valid email address")
        String email,
        String password
) {
}
