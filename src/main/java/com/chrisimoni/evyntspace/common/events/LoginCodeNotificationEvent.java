package com.chrisimoni.evyntspace.common.events;

public class LoginCodeNotificationEvent extends CodeNotificationEvent {
    public LoginCodeNotificationEvent(Object source, String recipient, String verificationCode, int codeValidityInMinutes) {
        super(source, recipient, verificationCode, codeValidityInMinutes);
    }
}