package com.chrisimoni.evyntspace.event.model;

import com.chrisimoni.evyntspace.event.enums.OnlinePlatformType;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class OnlineEventDetails {
    private OnlinePlatformType onlinePlatform; // e.g., "Zoom", "Google Meet"
    private String meetingLink;
}
