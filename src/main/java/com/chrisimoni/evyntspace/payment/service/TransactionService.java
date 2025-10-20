package com.chrisimoni.evyntspace.payment.service;

import com.chrisimoni.evyntspace.payment.enums.TransactionStatus;
import com.chrisimoni.evyntspace.payment.model.Transaction;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface TransactionService {
    Transaction createTransaction(String paymentIntentId, BigDecimal amount, String currency, TransactionStatus status, String accountId);
    Optional<Transaction> getTransactionById(UUID transactionId);
}