package com.chrisimoni.evyntspace.notification.model;

import com.chrisimoni.evyntspace.common.model.BaseEntity;
import com.chrisimoni.evyntspace.notification.enums.NotificationStatus;
import com.chrisimoni.evyntspace.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "notification_outbox")
@Getter
@Setter
@NoArgsConstructor
public class NotificationOutbox extends BaseEntity {
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private MessageDetails messageDetails;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private int retryAttempts;
    private Instant lastAttemptTime;
    private String lastError;
    private Instant nextAttemptTime;

    public NotificationOutbox(MessageDetails messageDetails, NotificationType notificationType) {
        this.messageDetails = messageDetails;
        this.notificationType = notificationType;
        this.status = NotificationStatus.PENDING;
        this.retryAttempts = 0;
        this.nextAttemptTime = Instant.now();
    }

    // Method to mark as failed for retry
    public void markAsFailed(String error, Instant nextAttemptTime) {
        this.status = NotificationStatus.FAILED;
        this.lastError = setErrorMessage(error);
        this.nextAttemptTime = nextAttemptTime;
    }

    // Method to mark as sent
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.lastAttemptTime = Instant.now();
        this.nextAttemptTime = null; // No further attempts needed
    }

    // Method to mark as permanently failed
    public void markPermanentFailure(String error) {
        this.status = NotificationStatus.PERMANENT_FAILURE;
        this.lastError = setErrorMessage(error);
        this.nextAttemptTime = null; // No further attempts
    }

    private String setErrorMessage(String error) {
        return Objects.nonNull(error)
                ? error.substring(0, Math.min(error.length(), 255))
                : String.format(
                "Unknown error while sending %s notification",
                this.notificationType.name().toLowerCase());
    }
}
