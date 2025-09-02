package com.chrisimoni.evyntspace.event.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record EnrollmentRequest(
        @NotNull(message = "Event ID cannot be null")
        UUID eventId,
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email must be a valid email address")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,
        @NotBlank(message = "First name cannot be empty")
        @Size(min = 2, max = 20, message = "First name must be between 2 and 50 characters")
        String firstName,
        @NotBlank(message = "Last name cannot be empty")
        @Size(min = 2, max = 20, message = "Last name must be between 2 and 50 characters")
        String lastName
){}
