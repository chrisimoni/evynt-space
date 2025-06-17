package com.chrisimoni.evyntspace.event.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class PhysicalEventDetails {
    private String venueName;
    private String address;
    private String city;
    private String state;
    private String country;
}
