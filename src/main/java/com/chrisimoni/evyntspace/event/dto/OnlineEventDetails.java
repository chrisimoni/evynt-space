package com.chrisimoni.evyntspace.event.dto;

import com.chrisimoni.evyntspace.event.enums.OnlinePlatformType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OnlineEventDetails(
        @NotNull(message = "Online platform cannot be null for online events.")
        OnlinePlatformType onlinePlatform,
        @NotBlank(message = "Meeting link cannot be empty")
        String meetingLink
) {
}
