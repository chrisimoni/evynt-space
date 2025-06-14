package com.chrisimoni.evyntspace.notification.model;

import com.chrisimoni.evyntspace.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_outbox")
@Getter
@Setter
@NoArgsConstructor
public class NotificationOutbox extends BaseEntity {
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private MessageDetails messageDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private int retryAttempts = 0;

    private LocalDateTime lastAttemptTime;
    private String lastError;
    private LocalDateTime nextAttemptTime;

    public NotificationOutbox(MessageDetails messageDetails, NotificationType notificationType) {
        this.messageDetails = messageDetails;
        this.notificationType = notificationType;
        this.status = NotificationStatus.PENDING;
    }
}
