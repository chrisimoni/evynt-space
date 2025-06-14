package com.chrisimoni.evyntspace.notification.service.email.gateway;

import com.chrisimoni.evyntspace.notification.model.MessageDetails;

public interface EmailServiceGateway {
    void sendEmail(MessageDetails messageDetails);
}
