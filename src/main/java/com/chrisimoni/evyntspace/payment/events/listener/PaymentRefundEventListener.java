package com.chrisimoni.evyntspace.payment.events.listener;

import com.chrisimoni.evyntspace.event.events.PaymentRefundEvent;
import com.chrisimoni.evyntspace.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRefundEventListener {
    private final PaymentService paymentService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePaymentRefundEvent(PaymentRefundEvent event) {
        log.info("PaymentRefundEvent received for transaction ID: {}.", event.getTransactionId());
        paymentService.initiateRefund(event.getUserId(), event.getTransactionId());
    }
}
