package com.chrisimoni.evyntspace.payment.service.impl;

import com.chrisimoni.evyntspace.common.exception.ExternalServiceException;
import com.chrisimoni.evyntspace.event.enums.PaymentStatus;
import com.chrisimoni.evyntspace.payment.events.PaymentConfirmationEvent;
import com.chrisimoni.evyntspace.payment.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class StripePaymentServiceImpl implements PaymentService {
    private final ApplicationEventPublisher eventPublisher;

    @Value("${spring.application.base-url}")
    private String baseUrl;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private record WebhookData(String reservationNumber, String paymentReference) {}

    public StripePaymentServiceImpl(@Value("${stripe.api.secret-key}") String secretKey, ApplicationEventPublisher eventPublisher) {
        Stripe.apiKey = secretKey; // initialize Stripe SDK
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String createCheckoutSession(
            String reservationNumber, String customerEmail, String eventTitle, BigDecimal amount, String eventImageUrl) {
        try {
            String successUrl = baseUrl + "/checkout/success";
            String cancelUrl = baseUrl + "/checkout/cancel";

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setCustomerEmail(customerEmail)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .putMetadata("reservationNumber", reservationNumber)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount(convertAmountToCent(amount))
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(eventTitle)
                                            //.putMetadata("reservationNumber", reservationNumber)
                                            .addImage(eventImageUrl)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            throw new ExternalServiceException("Failed to create Stripe checkout session", e);
        }
    }

    @Override
    public void handleStripeWebhook(String payload, String sigHeader) {
        Event event = verifySignature(payload, sigHeader);

        WebhookData data = extractDataFromEvent(event);
        if (data == null) {
            log.warn("Webhook event received without necessary data. Event Type: {}", event.getType());
            return;
        }

        PaymentStatus paymentStatus = getPaymentStatus(event.getType());
        if (paymentStatus == null) {
            log.warn("Webhook event type is not handled. Event Type: {}", event.getType());
            return;
        }

        eventPublisher.publishEvent(new PaymentConfirmationEvent(this, data.reservationNumber(), paymentStatus, data.paymentReference()));
        log.info("Successfully published a PaymentConfirmationEvent for reservation: {}", data.reservationNumber());
    }

    @Override
    @Async
    public void initiateRefund(String paymentIntentId) {
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .build();

        try {
            Refund.create(params);
            log.info("Successfully initiated refund for Payment Intent: {}", paymentIntentId);
        } catch (StripeException e) {
            log.error("Error occurred while initiating refund: {}", e.getMessage(), e);
            throw new ExternalServiceException("Error occurred while initiating refund", e);
        }
    }

    private Event verifySignature(String payload, String sigHeader) {
        try {
            return Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe signature: {}", e.getMessage(), e);
            throw new ExternalServiceException("Invalid stripe signature", e);
        }
    }

    private WebhookData extractDataFromEvent(Event event) {
        Optional<StripeObject> objectOptional = event.getDataObjectDeserializer().getObject();
        if (objectOptional.isEmpty()) {
            return null;
        }

        StripeObject stripeObject = objectOptional.get();

        String reservationNumber = null;
        String paymentReference = null;

        if (stripeObject instanceof Session session) {
            reservationNumber = session.getMetadata().get("reservationNumber");
            paymentReference = session.getPaymentIntent();
        } else if (stripeObject instanceof PaymentIntent paymentIntent) {
            reservationNumber = paymentIntent.getMetadata().get("reservationNumber");
            paymentReference = paymentIntent.getId();
        }

        if (reservationNumber == null || paymentReference == null) {
            log.warn("Webhook event missing critical metadata. ReservationNumber or Payment Reference");
            return null;
        }

        return new WebhookData(reservationNumber, paymentReference);
    }

    private PaymentStatus getPaymentStatus(String type) {
        return switch (type) {
            case "checkout.session.completed" -> PaymentStatus.CONFIRMED;
            case "payment_intent.payment_failed" -> PaymentStatus.FAILED;
            case "checkout.session.expired",
                 "payment_intent.canceled" -> PaymentStatus.CANCELED;
            default -> PaymentStatus.FAILED; // Return FAILED for unhandled events
        };
    }

    private long convertAmountToCent(BigDecimal amount) {
        // Multiply by 100 to get the value in cents
        BigDecimal priceInCents = amount.multiply(new BigDecimal("100"));
        // Convert to a long integer
        return priceInCents.longValueExact();
    }
}
