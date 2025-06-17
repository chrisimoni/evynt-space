package com.chrisimoni.evyntspace.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerificationConfirmRequest(
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email must be a valid email address")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,
        @NotBlank(message = "Code cannot be empty")
        String code
) {
}
