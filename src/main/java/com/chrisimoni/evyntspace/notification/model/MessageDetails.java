package com.chrisimoni.evyntspace.notification.model;

import com.chrisimoni.evyntspace.notification.enums.MessageTemplate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class MessageDetails implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String recipient;
    private String subject;
    private String body;
    private String recipientName;
    private MessageTemplate messageTemplate;
    private Map<String, Object> templateModel;

    public MessageDetails(String recipient, MessageTemplate messageTemplate, Map<String, Object> templateModel) {
        this.recipient = recipient;
        this.messageTemplate = messageTemplate;
        this.templateModel = templateModel;
    }
}
