package com.chrisimoni.evyntspace.payment.service.impl;

import com.chrisimoni.evyntspace.common.exception.ExternalServiceException;
import com.chrisimoni.evyntspace.event.enums.PaymentStatus;
import com.chrisimoni.evyntspace.payment.dto.StripeOnboardingResponse;
import com.chrisimoni.evyntspace.payment.enums.PaymentPlatform;
import com.chrisimoni.evyntspace.payment.events.PaymentConfirmationEvent;
import com.chrisimoni.evyntspace.payment.model.PaymentAccount;
import com.chrisimoni.evyntspace.payment.service.PaymentAccountService;
import com.chrisimoni.evyntspace.payment.service.PaymentService;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.service.UserService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.chrisimoni.evyntspace.payment.util.PaymentUtil.convertAmountToCent;
import static com.chrisimoni.evyntspace.payment.util.PaymentUtil.getCountryCode;

@Service
@Slf4j
public class StripePaymentServiceImpl implements PaymentService {
    private final PaymentAccountService paymentAccountService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;

    @Value("${spring.application.base-url}")
    private String baseUrl;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private record WebhookData(String reservationNumber, String paymentReference) {}

    public StripePaymentServiceImpl(
            @Value("${stripe.api.secret-key}") String secretKey,
            PaymentAccountService paymentAccountService,
            UserService userService,
            ApplicationEventPublisher eventPublisher) {
        Stripe.apiKey = secretKey; // initialize Stripe SDK
        this.paymentAccountService = paymentAccountService;
        this.userService = userService;
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

    @Override
    @Transactional
    public StripeOnboardingResponse createAndOnboardStripeAccount(UUID userId) {
        return paymentAccountService.findByUserIdAndPlatform(userId, PaymentPlatform.STRIPE)
                .map(this::onboardExistingAccount)
                .orElseGet(() -> createNewStripeAccount(userId));
    }

    private StripeOnboardingResponse onboardExistingAccount(PaymentAccount account) {
        try {
            String linkUrl = createAccountLink(account.getAccountId());
            return new StripeOnboardingResponse(account.getAccountId(), linkUrl);
        } catch (StripeException e) {
            throw new ExternalServiceException("Failed to create account link", e);
        }
    }

    private StripeOnboardingResponse createNewStripeAccount(UUID userId) {
        try {
            User user = userService.findById(userId);

            final String stripeAccountId = createConnectAccount(
                    userId.toString(), user.getEmail(), "US"
            );

            PaymentAccount account = new PaymentAccount();
            account.setAccountId(stripeAccountId);
            account.setUser(user);
            account.setPlatformName(PaymentPlatform.STRIPE);
            paymentAccountService.save(account);

            final String accountLinkUrl = createAccountLink(stripeAccountId);

            return new StripeOnboardingResponse(stripeAccountId, accountLinkUrl);

        } catch (StripeException e) {
            throw new ExternalServiceException("Failed to create Stripe account", e);
        }
    }

    private String createConnectAccount(String userId, String organizerEmail, String country) throws StripeException {
        AccountCreateParams.Capabilities capabilities = AccountCreateParams.Capabilities.builder()
                .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder()
                        .setRequested(true)
                        .build())
                .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                        .setRequested(true)
                        .build())
                .build();

        AccountCreateParams params = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.STANDARD)
                .setEmail(organizerEmail)
                .setCountry(getCountryCode(country))
                .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)
                .setCapabilities(capabilities)
                .putMetadata("platform_user_id", userId)
                .build();

        Account account = Account.create(params);
        log.info("Successfully created Stripe connect account for: {}", organizerEmail);
        return account.getId();
    }

    private String createAccountLink(String accountId) throws StripeException {
        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(accountId)
                .setRefreshUrl("https://your-app.com/stripe/refresh-onboarding")
                .setReturnUrl("https://your-app.com/stripe/onboarding-complete")
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink accountLink = AccountLink.create(params);
        log.info("Successfully created Stripe connect account link");
        return accountLink.getUrl();
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

    @Override
    public void handleStripeConnectAccountWebhook(String payload, String sigHeader) {
        Event event = verifySignature(payload, sigHeader);
        if ("account.updated".equals(event.getType())) {
            Optional<StripeObject> objectOptional = event.getDataObjectDeserializer().getObject();
            if (objectOptional.isEmpty()) {
                return;
            }

            StripeObject stripeObject = objectOptional.get();
            if (stripeObject instanceof Account stripeAccount) {
                String accountId = stripeAccount.getId();

                // Check for charges_enabled and payouts_enabled
                Boolean chargesEnabled = stripeAccount.getChargesEnabled();
                Boolean payoutsEnabled = stripeAccount.getPayoutsEnabled();

                // Retrieve the corresponding PaymentAccount from your database
                PaymentAccount paymentAccount = paymentAccountService.findByAccountId(accountId);

                if (paymentAccount != null) {
                    // Update the flags based on the webhook event
                    paymentAccount.setChargesEnabled(chargesEnabled);
                    paymentAccount.setPayoutsEnabled(payoutsEnabled);

                    paymentAccountService.save(paymentAccount);
                    log.info("Updated PaymentAccount for ID: {} with chargesEnabled: {} and payoutsEnabled: {}",
                            accountId, chargesEnabled, payoutsEnabled);
                } else {
                    log.warn("Received webhook for unknown account ID: {}", accountId);
                }
            }
        }
    }
}
