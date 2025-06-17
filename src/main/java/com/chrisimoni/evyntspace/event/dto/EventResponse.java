package com.chrisimoni.evyntspace.event.dto;

import com.chrisimoni.evyntspace.event.enums.EventType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        String title,
        String summary,
        String description,
        EventType eventType,
        PhysicalEventDetails physicalEventDetails,
        OnlineEventDetails onlineEventDetails,
        BigDecimal price,
        String eventImageUrl,
        Instant startDate,
        Instant endDate,
        OrganizerDetails organizer
) {
}
