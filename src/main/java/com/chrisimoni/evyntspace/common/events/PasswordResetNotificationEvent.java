package com.chrisimoni.evyntspace.common.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PasswordResetNotificationEvent extends ApplicationEvent {
    private final String recipient;
    private final String link;
    private final int validity;

    public PasswordResetNotificationEvent(Object source, String recipient, String link, int validity) {
        super(source);
        this.recipient = recipient;
        this.link = link;
        this.validity = validity;
    }
}
