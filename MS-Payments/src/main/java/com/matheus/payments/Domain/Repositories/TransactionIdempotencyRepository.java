package com.matheus.payments.Domain.Repositories;

import com.matheus.payments.Domain.Models.TransactionIdempotency;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionIdempotencyRepository {
    Optional<TransactionIdempotency> getByTransactionId(UUID transactionId);

    void save(TransactionIdempotency transaction);
}
