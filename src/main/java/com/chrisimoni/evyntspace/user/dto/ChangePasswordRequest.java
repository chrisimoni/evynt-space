package com.chrisimoni.evyntspace.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password cannot be empty")
        String currentPassword,
        @NotBlank(message = "New password cannot be empty")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        String newPassword
) {
}
