package com.chrisimoni.evyntspace.event.dto;

import com.chrisimoni.evyntspace.event.enums.EventType;
import com.chrisimoni.evyntspace.event.validator.ValidEventCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ValidEventCreateRequest
public record EventCreateRequest(
        @NotNull(message = "Organizer ID cannot be null")
        UUID organizerId,
        @NotBlank(message = "Event title cannot be empty.")
        @Size(max = 100, message = "Event title cannot exceed 100 characters.")
        String title,
        @NotBlank(message = "Event summary cannot be empty.")
        @Size(max = 150, message = "Event summary cannot exceed 150 characters.")
        String summary,
        @Size(max = 2000, message = "Event description cannot exceed 2000 characters.")
        String description,

        @NotNull(message = "Event type must be specified (ONLINE or PHYSICAL).")
        EventType eventType,
        // Use @Valid to trigger validation on nested DTOs
        @Valid
        PhysicalEventDetails physicalEventDetails, // Conditionally required based on eventType
        @Valid
        OnlineEventDetails onlineEventDetails, // Conditionally required based on eventType

        @NotNull(message = "Number of slots is required.")
        @Min(value = 1, message = "Number of slots must be at least 1.")
        Integer numberOfSlots,
        @NotNull(message = "isPaid field cannot be null")
        Boolean isPaid,
        // Price is optional, for free events it can be null or 0
        @Min(value = 0, message = "Price cannot be negative.")
        BigDecimal price,
        String eventImageUrl,

        @NotNull(message = "Start date cannot be null")
        @FutureOrPresent(message = "Start date must be in the present or future")
        Instant startDate,
        @NotNull(message = "End date cannot be null")
        @Future(message = "End date must be in the future.")
        Instant endDate,
        @NotNull(message = "Start date cannot be null")
        @FutureOrPresent(message = "Start date must be in the present or future")
        Instant registrationCloseDate,

        @Valid
        List<AgendaDetails> agendas,

        @Future(message = "Scheduled publish date must be in the future.")
        Instant scheduledPublishDate
) {
}
