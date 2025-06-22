package com.chrisimoni.evyntspace.notification.service;

import com.chrisimoni.evyntspace.notification.model.MessageDetails;

public interface NotificationService {
    void send(MessageDetails messageDetails);
}
