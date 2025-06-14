package com.chrisimoni.evyntspace.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 2, max = 20, message = "First name must be between 2 and 50 characters")
        String firstName,
        @Size(min = 2, max = 20, message = "Last name must be between 2 and 50 characters")
        String lastName,
        String company,
        String phoneNumber,
        String profileImgUrl
) {
}
