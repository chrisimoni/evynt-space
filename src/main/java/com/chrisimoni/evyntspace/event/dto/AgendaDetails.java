package com.chrisimoni.evyntspace.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record AgendaDetails(
        @NotBlank(message = "Agenda title cannot be empty.")
        @Size(max = 100, message = "Agenda title cannot exceed 100 characters.")
        String title,
        String presenter,
        String presenterImageUrl,
        String description,
        Instant startTime,
        Instant endTime
) {
}
