package com.chrisimoni.evyntspace.notification.service.outbox;

import com.chrisimoni.evyntspace.notification.enums.NotificationStatus;
import com.chrisimoni.evyntspace.notification.model.MessageDetails;
import com.chrisimoni.evyntspace.notification.enums.NotificationType;
import com.chrisimoni.evyntspace.notification.model.NotificationOutbox;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationOutboxService {
    void saveFailedMessageToOutbox(MessageDetails messageDetails, NotificationType notificationType, String error);
    List<NotificationOutbox> findFailedMessagesToProcess(NotificationStatus notificationStatus, Pageable pageable);
    void processSingleOutboxMessage(NotificationOutbox message);
    int deleteOldProcessedRecords(int retentionDays);
}
