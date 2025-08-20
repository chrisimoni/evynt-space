package com.chrisimoni.evyntspace.notification.service.email.gateway;

import com.chrisimoni.evyntspace.common.exception.ExternalServiceException;
import com.chrisimoni.evyntspace.notification.model.MessageDetails;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "notification.email-delivery.method", havingValue = "smtp", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailServiceGatewayImpl implements EmailServiceGateway {
    private final JavaMailSender mailSender;

    @Value("${notification.email.sender}")
    private String sender;

    @Override
    @Retryable(
            retryFor = {MailSendException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000, multiplier = 2)
    )
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
        } catch (MailSendException e) {
            // This is a transient network error. It will be retried by @Retryable.
            log.warn("Failed to send email to recipient {}. Retrying...", messageDetails.getRecipient(), e);
            throw e;
        } catch (MailAuthenticationException | MessagingException e) {
            log.error("Email sending failed permanently due to a messaging error for recipient {}: {}", messageDetails.getRecipient(), e.getMessage(), e);
            throw new RuntimeException("Permanent failure sending email.", e);
        }
    }

    // The recover method is called only after all retry attempts have failed
    @Recover
    public void recover(MailSendException e, MessageDetails messageDetails) {
        // This is a fallback to guarantee delivery
        log.error("Final email sending failed for recipient {} after all retries.",
                messageDetails.getRecipient(), e);
        throw new ExternalServiceException("Failed to send email after retries.", e);
    }
}
