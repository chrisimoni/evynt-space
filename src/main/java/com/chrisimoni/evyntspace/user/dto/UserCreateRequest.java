package com.chrisimoni.evyntspace.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserCreateRequest(
        @NotNull(message = "Verification token is required to create a user.")
        UUID verificationToken,
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email must be a valid email address")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,
        @NotBlank(message = "First name cannot be empty")
        @Size(min = 2, max = 20, message = "First name must be between 2 and 50 characters")
        String firstName,
        @NotBlank(message = "Last name cannot be empty")
        @Size(min = 2, max = 20, message = "Last name must be between 2 and 50 characters")
        String lastName,
        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,
        String company,
        String phoneNumber,
        @NotBlank(message = "Country code cannot be empty")
        @Size(min = 2, max = 2, message = "Country code must be 2 characters")
        String countryCode
) {
}
