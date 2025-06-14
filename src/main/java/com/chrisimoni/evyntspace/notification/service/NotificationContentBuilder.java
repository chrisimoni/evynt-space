package com.chrisimoni.evyntspace.notification.service;

import com.chrisimoni.evyntspace.notification.model.MessageDetails;
import com.chrisimoni.evyntspace.notification.model.MessageTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static com.chrisimoni.evyntspace.notification.constant.NotificationTemplateConstants.VERIFICATION_CODE_KEY;

@Service
@RequiredArgsConstructor
public class NotificationContentBuilder {
    private final SpringTemplateEngine templateEngine; // For Thymeleaf

    public MessageDetails createMessageDetails(String recipient, MessageTemplate messageTemplate, Map<String, Object> templateModel) {
        MessageDetails messageDetails = new MessageDetails(recipient, messageTemplate, templateModel);
        String subject = getSubjectForTemplate(
                messageDetails.getMessageTemplate(), messageDetails.getTemplateModel());
        String body = buildHtmlBody(messageDetails.getMessageTemplate(), messageDetails.getTemplateModel());
        messageDetails.setSubject(subject);
        messageDetails.setBody(body);

        return messageDetails;
    }

    private String buildHtmlBody(MessageTemplate messageTemplate, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        String emailTemplate = messageTemplate.name()
                .toLowerCase()
                .replace("notification", "email")
                .replace('_', '-');
        return templateEngine.process("emails/" + emailTemplate, context);
    }

    private String getSubjectForTemplate(MessageTemplate messageTemplate, Map<String, Object> model) {
        return switch (messageTemplate) {
            case VERIFICATION_NOTIFICATION -> "Verify Your Account - "+ model.get(VERIFICATION_CODE_KEY);
            case PASSWORD_RESET_NOTIFICATION -> "Password Reset Request";
            default -> "Important Notification";
        };
    }
}
