package com.chrisimoni.evyntspace.event.model;

import com.chrisimoni.evyntspace.event.enums.OnlinePlatformType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class OnlineEventDetails {
    @Enumerated(EnumType.STRING)
    private OnlinePlatformType onlinePlatform;
    private String meetingLink;
}
