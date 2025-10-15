package com.chrisimoni.evyntspace.payment.service.impl;

import com.chrisimoni.evyntspace.common.exception.BadRequestException;
import com.chrisimoni.evyntspace.common.exception.ExternalServiceException;
import com.chrisimoni.evyntspace.event.enums.PaymentStatus;
import com.chrisimoni.evyntspace.payment.dto.StripeOnboardingResponse;
import com.chrisimoni.evyntspace.payment.enums.CurrencyType;
import com.chrisimoni.evyntspace.payment.enums.PaymentPlatform;
import com.chrisimoni.evyntspace.payment.enums.TransactionStatus;
import com.chrisimoni.evyntspace.payment.events.PaymentConfirmationEvent;
import com.chrisimoni.evyntspace.payment.model.PaymentAccount;
import com.chrisimoni.evyntspace.payment.model.Transaction;
import com.chrisimoni.evyntspace.payment.service.PaymentAccountService;
import com.chrisimoni.evyntspace.payment.service.PaymentService;
import com.chrisimoni.evyntspace.payment.service.TransactionService;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.service.UserService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.chrisimoni.evyntspace.payment.util.PaymentUtil.*;

@Service
@Slf4j
public class StripePaymentServiceImpl implements PaymentService {
    private final PaymentAccountService paymentAccountService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;
    private final TransactionService transactionService;

    @Value("${spring.application.base-url}")
    private String baseUrl;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${stripe.webhook.connect-secret}")
    private String connectWebhookSecret;

    @Value("${platform-fee-percentage}")
    private int platformFeePercentage;

    public StripePaymentServiceImpl(
            @Value("${stripe.api.secret-key}") String secretKey,
            PaymentAccountService paymentAccountService,
            UserService userService,
            TransactionService transactionService,
            ApplicationEventPublisher eventPublisher) {
        Stripe.apiKey = secretKey; // initialize Stripe SDK
        this.paymentAccountService = paymentAccountService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.eventPublisher = eventPublisher;
    }

    //INITIAL IMPL WITHOUT DIRECT CHARGE
//    @Override
//    public String createCheckoutSession(
//            String reservationNumber, String customerEmail, String eventTitle, BigDecimal amount, String eventImageUrl) {
//        try {
//            String successUrl = baseUrl + "/checkout/success";
//            String cancelUrl = baseUrl + "/checkout/cancel";
//
//            // Define the metadata needed for the Payment Intent
//            Map<String, String> paymentIntentMetadata = Map.of(
//                    "reservationNumber", reservationNumber
//            );
//
//            SessionCreateParams params = SessionCreateParams.builder()
//                    .setMode(SessionCreateParams.Mode.PAYMENT)
//                    .setCustomerEmail(customerEmail)
//                    .setSuccessUrl(successUrl)
//                    .setCancelUrl(cancelUrl)
//
//                    // Keep metadata on the Checkout Session (for checkout.session.completed)
//                    .putMetadata("reservationNumber", reservationNumber)
//
//                    // This block instructs Stripe to copy the metadata onto the resulting Payment Intent.
//                    .setPaymentIntentData(
//                            SessionCreateParams.PaymentIntentData.builder()
//                                    .putAllMetadata(paymentIntentMetadata) // Attach metadata to the Payment Intent
//                                    .build()
//                    )
//                    .addLineItem(SessionCreateParams.LineItem.builder()
//                            .setQuantity(1L)
//                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
//                                    .setCurrency(CurrencyType.USD.name().toLowerCase())
//                                    .setUnitAmount(convertAmountToCent(amount))
//                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
//                                            .setName(eventTitle)
//                                            .addImage(eventImageUrl)
//                                            .build()
//                                    )
//                                    .build()
//                            )
//                            .build()
//                    )
//                    .build();
//
//            Session session = Session.create(params);
//            return session.getUrl();
//        } catch (StripeException e) {
//            throw new ExternalServiceException("Failed to create Stripe checkout session", e);
//        }
//    }

    public String createCheckoutSession(
            UUID userId,
            String reservationNumber,
            String customerEmail,
            String eventTitle,
            BigDecimal amount,
            String eventImageUrl) { // Added missing parameter for clarity
        try {
            Optional<PaymentAccount> paymentAccountOptional = paymentAccountService.findByUserId(userId);
            if(paymentAccountOptional.isEmpty()) {
                throw new BadRequestException("This event is currently unavailable for enrollment. " +
                        "The organizer needs to complete their payment setup. " +
                        "Please try again later or contact the event organizer.");
            }

            String accountId = paymentAccountOptional.get().getAccountId();

            String successUrl = baseUrl + "/checkout/success";
            String cancelUrl = baseUrl + "/checkout/cancel";

            // Convert platform fee to the lowest currency unit (cents/pennies)
            // Ensure you have access to platformFeePercentage here
            Long platformFeeInCents = calculatePlatformFee(amount, platformFeePercentage);

            // Define the metadata needed for the Payment Intent
            Map<String, String> metadataMap = Map.of(
                    "reservationNumber", reservationNumber
            );

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setCustomerEmail(customerEmail)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .putAllMetadata(metadataMap)
                    .setPaymentIntentData(
                            SessionCreateParams.PaymentIntentData.builder()
                                    .putAllMetadata(metadataMap)

                                    // Specify Platform's Application Fee
                                    .setApplicationFeeAmount(platformFeeInCents)
                                    .build()
                    )
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(CurrencyType.USD.name().toLowerCase())
                                    .setUnitAmount(convertAmountToCent(amount))
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(eventTitle)
                                            .addImage(eventImageUrl)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build();

            // CRITICAL: This header makes the charge a Direct Charge.
            RequestOptions options = RequestOptions.builder()
                    .setStripeAccount(accountId)
                    .build();

            Session session = Session.create(params, options);
            return session.getUrl();
        } catch (StripeException e) {
            log.error("Failed to create Stripe checkout session -> {}", e.getMessage(), e);
            throw new ExternalServiceException("Failed to create Stripe checkout session", e);
        }
    }

    @Override
    public void handleStripeWebhook(String payload, String sigHeader) {
        Event event = verifySignature(payload, sigHeader, webhookSecret);
        handleEvent(event);
    }

    private void handleEvent(Event event) {
        Object stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);

        if (stripeObject == null) {
            log.warn("Webhook event received with no data object. Event Type: {}", event.getType());
            return;
        }

        switch (event.getType()) {
            case "account.updated" -> handleAccountUpdated((Account) stripeObject);
            case "checkout.session.completed" -> handleCheckoutSessionCompleted((Session) stripeObject);
            case "payment_intent.payment_failed" -> handlePaymentIntentFailed((PaymentIntent) stripeObject);
            case "checkout.session.expired" -> handleCheckoutSessionExpired((Session) stripeObject);
            case "payment_intent.canceled" -> handlePaymentIntentCanceled((PaymentIntent) stripeObject);
            case "charge.refunded" -> handleChargeRefunded((Charge) stripeObject, event.getAccount());
            default -> log.info("Webhook event type not handled: {}", event.getType());
        }
    }

    private void handleChargeRefunded(Charge charge, String accountId) {
        String paymentIntentId = charge.getPaymentIntent();
        String reservationNumber = charge.getMetadata().get("reservationNumber");

        if (paymentIntentId == null || paymentIntentId.isEmpty()) {
            log.warn("Charge {} on account {} refunded but is missing paymentIntent ID.",
                    charge.getId(), accountId);
            return;
        }

        Long amount = charge.getAmount();
        String currency = charge.getCurrency();

        processTransactionAndConfirmEvent(
                reservationNumber,
                paymentIntentId,
                amount,
                currency,
                TransactionStatus.REFUNDED,
                PaymentStatus.REFUNDED
        );
    }

    private void handleAccountUpdated(Account stripeAccount) {
        String accountId = stripeAccount.getId();

        // Check for charges_enabled and payouts_enabled
        Boolean chargesEnabled = stripeAccount.getChargesEnabled();
        Boolean payoutsEnabled = stripeAccount.getPayoutsEnabled();

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

    private void handleCheckoutSessionCompleted(Session session) {
        String reservationNumber = session.getMetadata().get("reservationNumber");
        String paymentIntentId = session.getPaymentIntent();
        long amount = session.getAmountTotal();
        String currency = session.getCurrency();

        if (reservationNumber == null || paymentIntentId == null) {
            log.warn("Missing metadata in checkout.session.completed event.");
            return;
        }

        processTransactionAndConfirmEvent(
                reservationNumber,
                paymentIntentId,
                amount,
                currency,
                TransactionStatus.SUCCEEDED,
                PaymentStatus.CONFIRMED
        );

    }

    private void handlePaymentIntentFailed(PaymentIntent paymentIntent) {
        String paymentIntentId = paymentIntent.getId();
        String reservationNumber = paymentIntent.getMetadata().get("reservationNumber");

        if (reservationNumber == null) {
            log.error("PaymentIntent {} failed, but reservation number metadata is missing.", paymentIntentId);
            return;
        }

        Long amount = paymentIntent.getAmount();
        String currency = paymentIntent.getCurrency();

        processTransactionAndConfirmEvent(
                reservationNumber,
                paymentIntentId,
                amount,
                currency,
                TransactionStatus.FAILED,
                PaymentStatus.FAILED
        );
    }

    private void handleCheckoutSessionExpired(Session session) {
        String reservationNumber = session.getMetadata().get("reservationNumber");
        if (reservationNumber == null) {
            log.warn("Missing reservation number in expired checkout session.");
            return;
        }

        processTransactionAndConfirmEvent(
                reservationNumber,
                null,
                null,
                null,
                null,
                PaymentStatus.CANCELED
        );
    }

    private void handlePaymentIntentCanceled(PaymentIntent paymentIntent) {
        String paymentIntentId = paymentIntent.getId();
        String reservationNumber = paymentIntent.getMetadata().get("reservationNumber");

        if (reservationNumber == null) {
            log.warn("PaymentIntent {} was canceled, but the reservation number metadata is missing.", paymentIntentId);
            return;
        }

        Long amount = paymentIntent.getAmount();
        String currency = paymentIntent.getCurrency();

        processTransactionAndConfirmEvent(
                reservationNumber,
                paymentIntentId,
                amount,
                currency,
                TransactionStatus.CANCELED,
                PaymentStatus.CANCELED
        );
    }

    //Refund on plaftform account, kept for reference
//    @Override
//    @Transactional
//    public void initiateRefund(UUID transactionId) {
//        Optional<Transaction> transactionOptional = transactionService.getTransactionById(transactionId);
//        if(transactionOptional.isEmpty()) {
//            log.info("No transaction found with the id: {}", transactionId);
//            return;
//        }
//
//        Transaction existingTransaction = transactionOptional.get();
//
//        String paymentIntentId = existingTransaction.getPaymentReferenceId();
//
//        RefundCreateParams params = RefundCreateParams.builder()
//                .setPaymentIntent(paymentIntentId)
//                .build();
//
//        try {
//            Refund.create(params);
//            log.info("Successfully initiated refund for Payment Intent: {}", paymentIntentId);
//            existingTransaction.setStatus(TransactionStatus.REFUNDED);
//            transactionService.updateTransaction(existingTransaction);
//        } catch (StripeException e) {
//            log.error("Error occurred while initiating refund: {}", e.getMessage(), e);
//            throw new ExternalServiceException("Error occurred while initiating refund", e);
//        }
//    }

    @Override
    @Transactional
    public void initiateRefund(UUID userId, UUID transactionId) {
        Optional<Transaction> transactionOptional = transactionService.getTransactionById(transactionId);
        if(transactionOptional.isEmpty()) {
            log.info("No transaction found with the id: {}", transactionId);
            return;
        }

        Optional<PaymentAccount> paymentAccountOptional  = paymentAccountService.findByUserId(userId);
        if(paymentAccountOptional.isEmpty()) {
            log.error("No payment account found for user with the id: {}", userId);
            return;
        }

        String connectedAccountId = paymentAccountOptional.get().getAccountId();

        String paymentIntentId = transactionOptional.get().getPaymentReferenceId();

        // Prepare Refund Parameters
        RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                // Optional: If you want to refund your platform fee, you must include this.
                // NOTE: This transfers the fee back from your platform balance to the organizer's account.
                .setRefundApplicationFee(true)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER); // Recommended reason

        // Prepare Request Options (CRITICAL for Direct Charges)
        RequestOptions options = RequestOptions.builder()
                .setStripeAccount(connectedAccountId) // Executes the refund against the organizer's account
                .build();

        try {
            Refund refund = Refund.create(paramsBuilder.build(), options);

            log.info("Successfully initiated refund {} for Payment Intent: {} on Account: {}",
                    refund.getId(), paymentIntentId, connectedAccountId);
        } catch (StripeException e) {
            log.error("Error occurred while initiating refund: {}", e.getMessage(), e);
            throw new ExternalServiceException("Error occurred while initiating refund", e);
        }
    }

    @Override
    @Transactional
    public StripeOnboardingResponse createAndOnboardStripeAccount(UUID userId) {
        return paymentAccountService.findByUserId(userId)
                .map(account -> {
                    // If the account is already enabled, return a DTO without a link.
                    if (Boolean.TRUE.equals(account.isChargesEnabled()) && Boolean.TRUE.equals(account.isPayoutsEnabled())) {
                        return new StripeOnboardingResponse(account.getAccountId(), null); // Or an empty string
                    }
                    // If not, proceed to generate the onboarding link.
                    return onboardExistingAccount(account);
                })
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
                    userId.toString(), user.getEmail(), user.getCountryCode()
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
                .setCountry(country)
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
                .setRefreshUrl("http://localhost:8080/payment/stripe/refresh-onboarding")
                .setReturnUrl("http://localhost:8080/payment/stripe/onboarding-complete")
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink accountLink = AccountLink.create(params);
        log.info("Successfully created Stripe connect account link");
        return accountLink.getUrl();
    }

    private Event verifySignature(String payload, String sigHeader, String webhookSecret) {
        try {
            return Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe signature: {}", e.getMessage(), e);
            throw new ExternalServiceException("Invalid stripe signature", e);
        }
    }

    @Override
    public void handleStripeConnectAccountWebhook(String payload, String sigHeader) {
        Event event = verifySignature(payload, sigHeader, connectWebhookSecret);
        handleEvent(event);
    }

    private void processTransactionAndConfirmEvent(
            String reservationNumber,
            String paymentIntentId,
            Long amount,
            String currency,
            TransactionStatus transactionStatus,
            PaymentStatus paymentStatus) {

        UUID transactionId = null;

        if(Objects.nonNull(paymentIntentId)) {
            Transaction transaction = transactionService.createTransaction(
                    paymentIntentId,
                    convertAmountToBigDecimal(amount),
                    currency,
                    transactionStatus
            );

            transactionId = transaction.getId();
        }

        triggerPaymentConfirmationEvent(reservationNumber, paymentStatus, transactionId);
    }

    private void triggerPaymentConfirmationEvent(String reservationNumber, PaymentStatus paymentStatus, UUID transactionId) {
        eventPublisher.publishEvent(new PaymentConfirmationEvent(this, reservationNumber, paymentStatus, transactionId));
        log.info("Successfully published a PaymentConfirmationEvent for reservation: {}", reservationNumber);
    }
}
