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
@Table(name = "verification_codes")
@Getter
@Setter
@NoArgsConstructor
public class VerificationCode extends BaseEntity {
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String code;
    @Column(nullable = false)
    private LocalDateTime expirationTime;
    @Column(nullable = false)
    private boolean isUsed = false;

    public VerificationCode(String email, String code, LocalDateTime expirationTime) {
        this.email = email;
        this.code = code;
        this.expirationTime = expirationTime;
    }
}
