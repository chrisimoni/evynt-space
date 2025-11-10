package com.chrisimoni.evyntspace.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyCodeRequest(
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email must be a valid email address")
        String email,
        @NotBlank(message = "Code cannot be empty")
        String code
) {
}
