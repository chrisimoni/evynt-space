package com.chrisimoni.evyntspace.common.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class PaymentRefundEvent extends ApplicationEvent {
    private final UUID userId;
    private final UUID transactionId;

    public PaymentRefundEvent(Object source, UUID userId, UUID transactionId) {
        super(source);
        this.userId = userId;
        this.transactionId = transactionId;
    }
}
