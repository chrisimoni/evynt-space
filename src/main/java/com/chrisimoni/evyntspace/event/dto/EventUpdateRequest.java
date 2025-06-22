package com.chrisimoni.evyntspace.event.dto;

import com.chrisimoni.evyntspace.event.validator.ValidEventCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record EventUpdateRequest(
        @Size(max = 100, message = "Event title cannot exceed 100 characters.")
        String title,
        @Size(max = 150, message = "Event summary cannot exceed 150 characters.")
        String summary,
        @Size(max = 2000, message = "Event description cannot exceed 2000 characters.")
        String description,

        @Min(value = 1, message = "Number of slots must be at least 1.")
        Integer numberOfSlots,
        @Min(value = 0, message = "Price cannot be negative.")
        BigDecimal price,
        String eventImageUrl,

        @FutureOrPresent(message = "Start date must be in the present or future")
        Instant startDate,
        @Future(message = "End date must be in the future.")
        Instant endDate,

        @Valid
        List<AgendaDetails> agendas
) {
}
