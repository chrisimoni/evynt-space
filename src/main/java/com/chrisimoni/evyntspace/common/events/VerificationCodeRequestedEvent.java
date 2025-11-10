package com.chrisimoni.evyntspace.common.events;

public class VerificationCodeRequestedEvent extends CodeNotificationEvent {
    public VerificationCodeRequestedEvent(Object source, String recipient, String verificationCode, int codeValidityInMinutes) {
        super(source, recipient, verificationCode, codeValidityInMinutes);
    }
}
