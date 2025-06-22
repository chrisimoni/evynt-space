package com.chrisimoni.evyntspace.event.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Agenda {
    private String title;
    private String presenter;
    private String presenterImageUrl;
    private String description;
    private Instant startTime;
    private Instant endTime;
}
