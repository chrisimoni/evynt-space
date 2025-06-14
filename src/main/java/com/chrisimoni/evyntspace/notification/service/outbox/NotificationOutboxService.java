package com.chrisimoni.evyntspace.notification.service.outbox;

import com.chrisimoni.evyntspace.notification.model.MessageDetails;
import com.chrisimoni.evyntspace.notification.model.NotificationOutbox;
import com.chrisimoni.evyntspace.notification.model.NotificationType;

public interface NotificationOutboxService {
    void saveToOutbox(MessageDetails messageDetails, NotificationType notificationType, String error);
}
