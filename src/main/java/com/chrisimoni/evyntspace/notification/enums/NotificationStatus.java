package com.chrisimoni.evyntspace.notification.enums;

public enum NotificationStatus {
    PENDING,    // Picked up by sender, in process of sending
    SENT,       // Successfully sent
    FAILED,     // Failed to send, potentially retryable
    PERMANENT_FAILURE // Failed multiple times, or permanent error (e.g., invalid email)
}
