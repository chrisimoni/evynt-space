package com.chrisimoni.evyntspace.payment.service;

import com.chrisimoni.evyntspace.payment.enums.CurrencyType;
import com.chrisimoni.evyntspace.payment.enums.TransactionStatus;
import com.chrisimoni.evyntspace.payment.model.Transaction;

import java.util.Optional;
import java.util.UUID;

public interface TransactionService {
    Transaction createTransaction(String paymentIntentId, long amount, String currency, TransactionStatus status);
    Optional<Transaction> getTransactionById(UUID transactionId);
}
