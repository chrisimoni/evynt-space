package com.chrisimoni.evyntspace.payment.service.impl;

import com.chrisimoni.evyntspace.payment.enums.CurrencyType;
import com.chrisimoni.evyntspace.payment.enums.TransactionStatus;
import com.chrisimoni.evyntspace.payment.model.Transaction;
import com.chrisimoni.evyntspace.payment.repository.TransactionRepository;
import com.chrisimoni.evyntspace.payment.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository repository;

//    @Value("${platform-fee-percentage}")
//    private int platformFeePercentage;

    @Override
    @Transactional
    public Transaction createTransaction(String paymentIntentId, long amount, String currency, TransactionStatus status) {
        return createAndSaveTransaction(paymentIntentId, amount, currency, status);
    }

    @Override
    public Optional<Transaction> getTransactionById(UUID transactionId) {
        return repository.findById(transactionId);
    }

    private Transaction createAndSaveTransaction(
            String paymentIntentId, Long amountTotal, String currency, TransactionStatus status) {
        // Convert amount from cents/smallest unit to BigDecimal
        BigDecimal amount = BigDecimal.valueOf(amountTotal).movePointLeft(2);

        Transaction transaction = new Transaction();
        transaction.setPaymentReferenceId(paymentIntentId);
        transaction.setAmount(amount);
        transaction.setCurrency(currency.toUpperCase());
        transaction.setStatus(status);

        return repository.save(transaction);
    }
}
