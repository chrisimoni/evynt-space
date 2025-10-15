package com.chrisimoni.evyntspace.payment.service.impl;

import com.chrisimoni.evyntspace.common.exception.ResourceNotFoundException;
import com.chrisimoni.evyntspace.payment.enums.TransactionStatus;
import com.chrisimoni.evyntspace.payment.model.Transaction;
import com.chrisimoni.evyntspace.payment.repository.TransactionRepository;
import com.chrisimoni.evyntspace.payment.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository repository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Transaction createTransaction(String paymentIntentId, BigDecimal amount, String currency, TransactionStatus status) {
        Transaction transaction = new Transaction();
        transaction.setPaymentReferenceId(paymentIntentId);
        transaction.setAmount(amount);
        transaction.setCurrency(currency.toUpperCase());
        transaction.setStatus(status);

        return repository.save(transaction);
    }

    @Override
    public Optional<Transaction> getTransactionById(UUID transactionId) {
        return repository.findById(transactionId);
    }
}
