package com.chrisimoni.evyntspace.common.model;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public class ActivatableEntity extends BaseEntity {
    private boolean active = true;
    private Instant deactivatedAt;
}
