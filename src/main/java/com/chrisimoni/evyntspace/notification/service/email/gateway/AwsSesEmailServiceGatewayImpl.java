package com.chrisimoni.evyntspace.notification.service.email.gateway;

import com.chrisimoni.evyntspace.notification.model.MessageDetails;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "email.delivery.method", havingValue = "aws-ses")
public class AwsSesEmailServiceGatewayImpl implements EmailServiceGateway {
    @Override
    public void sendEmail(MessageDetails messageDetails) {
        //TODO: to be implement when deployed to AWS
    }
}
