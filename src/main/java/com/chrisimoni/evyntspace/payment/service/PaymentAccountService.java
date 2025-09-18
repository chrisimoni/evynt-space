package com.chrisimoni.evyntspace.payment.service;

import com.chrisimoni.evyntspace.payment.enums.PaymentPlatform;
import com.chrisimoni.evyntspace.payment.model.PaymentAccount;

import java.util.Optional;
import java.util.UUID;

public interface PaymentAccountService {
    Optional<PaymentAccount> findByUserId(UUID userId);
    PaymentAccount save(PaymentAccount paymentAccount);
    PaymentAccount findByAccountId(String accountId);
}
