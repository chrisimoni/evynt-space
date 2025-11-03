package com.chrisimoni.evyntspace.user.model;

import com.chrisimoni.evyntspace.common.model.BaseEntity;
import com.chrisimoni.evyntspace.user.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tokens")
public class Token extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private String token;
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;
    private Instant expiryDate;
    //TODO: remove used and revoked flags
    private boolean used;
    private boolean revoked;

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }

    public boolean isPasswordResetToken() {
        return tokenType == TokenType.PASSWORD_RESET_TOKEN;
    }

    public boolean isRefreshToken() {
        return tokenType == TokenType.REFRESH_TOKEN;
    }

}
