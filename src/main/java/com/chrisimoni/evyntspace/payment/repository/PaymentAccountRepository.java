package com.chrisimoni.evyntspace.payment.repository;

import com.chrisimoni.evyntspace.payment.enums.PaymentPlatform;
import com.chrisimoni.evyntspace.payment.model.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, UUID> {
    Optional<PaymentAccount> findByUserId(UUID user_id);
    PaymentAccount findByAccountId(String accountId);
}
