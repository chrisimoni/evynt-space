package com.chrisimoni.evyntspace.user.model;

import com.chrisimoni.evyntspace.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "verified_sessions")
@Getter
@Setter
@NoArgsConstructor
public class VerifiedSession extends BaseEntity {
    private String email;
    private Instant expirationTime;
    private boolean isUsed;

    public VerifiedSession(String email, Instant expirationTime) {
        this.email = email;
        this.expirationTime = expirationTime;
    }
}
