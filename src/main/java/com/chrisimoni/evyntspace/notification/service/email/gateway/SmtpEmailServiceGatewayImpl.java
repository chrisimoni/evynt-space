package com.chrisimoni.evyntspace.notification.service.email.gateway;

import com.chrisimoni.evyntspace.common.exception.ExternalServiceException;
import com.chrisimoni.evyntspace.notification.model.MessageDetails;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "email.delivery.method", havingValue = "smtp", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailServiceGatewayImpl implements EmailServiceGateway {
    private final JavaMailSender mailSender;

    @Value("${email.sender}")
    private String sender;

    @Override
    public void sendEmail(MessageDetails messageDetails) {
        try{
            MimeMessage message = mailSender.createMimeMessage();
            //true for multipart message (e.g., attachments, HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(sender);
            helper.setTo(messageDetails.getRecipient());
            helper.setSubject(messageDetails.getSubject());
            helper.setText(messageDetails.getBody(), true); //true indicates that the body is html

            mailSender.send(message);
        } catch (MailAuthenticationException e) {
            log.error("Email authentication failure: {}", e.getMessage(), e);
            throw new ExternalServiceException("Authentication failed for email service.", e);
        } catch (MailSendException e) {
            log.error("Failed to send email to recipient {}: {}", messageDetails.getRecipient(), e.getMessage(), e);
            throw new ExternalServiceException("Failed to send email to " + messageDetails.getRecipient(), e);
        } catch (Exception e) {
            log.error("An unexpected error occurred while sending email to recipient {}: {}",
                    messageDetails.getRecipient(), e.getMessage(), e);
            throw new ExternalServiceException("An unexpected error occurred during email sending.", e);
        }
    }
}
