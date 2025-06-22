package com.chrisimoni.evyntspace.user.model;

import com.chrisimoni.evyntspace.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "verification_codes")
@Getter
@Setter
@NoArgsConstructor
public class VerificationCode extends BaseEntity {
    private String email;
    private String code;
    private Instant expirationTime;
    private boolean isUsed;

    public VerificationCode(String email, String code, Instant expirationTime) {
        this.email = email;
        this.code = code;
        this.expirationTime = expirationTime;
    }
}
