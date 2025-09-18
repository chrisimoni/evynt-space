package com.chrisimoni.evyntspace.payment.service.impl;

import com.chrisimoni.evyntspace.payment.enums.PaymentPlatform;
import com.chrisimoni.evyntspace.payment.model.PaymentAccount;
import com.chrisimoni.evyntspace.payment.repository.PaymentAccountRepository;
import com.chrisimoni.evyntspace.payment.service.PaymentAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentAccountServiceImpl implements PaymentAccountService {
    private final PaymentAccountRepository paymentAccountRepository;

    @Override
    public Optional<PaymentAccount> findByUserId(UUID userId) {
        return paymentAccountRepository.findByUserId(userId);
    }

    @Override
    public PaymentAccount save(PaymentAccount paymentAccount) {
        return paymentAccountRepository.save(paymentAccount);
    }

    @Override
    public PaymentAccount findByAccountId(String accountId) {
        return paymentAccountRepository.findByAccountId(accountId);
    }
}
