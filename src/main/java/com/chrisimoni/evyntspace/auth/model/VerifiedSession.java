package com.chrisimoni.evyntspace.auth.model;

import com.chrisimoni.evyntspace.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "verified_sessions")
@Getter
@Setter
@NoArgsConstructor
public class VerifiedSession extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private int expirationTimeInMinutes;

    @Column(nullable = false)
    private LocalDateTime expirationTime; // How long this session is valid for user creation

    @Column(nullable = false)
    private boolean isUsed = false; // Mark true once used for user creation

    public VerifiedSession(String email, int expirationTimeInMinutes, LocalDateTime expirationTime) {
        this.email = email;
        this.expirationTimeInMinutes = expirationTimeInMinutes;
        this.expirationTime = expirationTime;
    }
}
