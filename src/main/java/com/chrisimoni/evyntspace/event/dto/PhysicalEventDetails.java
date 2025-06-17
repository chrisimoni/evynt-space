package com.chrisimoni.evyntspace.event.dto;

import jakarta.validation.constraints.NotBlank;

public record PhysicalEventDetails(
        @NotBlank(message = "Venue name cannot be empty for physical events.")
        String venueName,
        @NotBlank(message = "Address cannot be empty for physical events.")
        String address,
        @NotBlank(message = "City cannot be empty for physical events.")
        String city,
        @NotBlank(message = "State/Province cannot be empty for physical events.")
        String state,
        @NotBlank(message = "Country cannot be empty for physical events.")
        String country
) {
}
