package com.chrisimoni.evyntspace.event.dto;

import com.chrisimoni.evyntspace.event.enums.EventStatus;
import com.chrisimoni.evyntspace.event.enums.EventType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventResponse(
        UUID id,
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
        String slug,
        OrganizerDetails organizer,
        List<AgendaDetails> agendas,
        EventStatus status,
        Instant publishedDate,
        Instant createdAt,
        Instant updatedAt,
        boolean active,
        Instant deactivatedAt,
        boolean isPaid
) {
}
