package com.matheus.payments.Domain.Repositories;

import com.matheus.payments.Domain.Models.TransactionOutbox;

import java.util.List;
import java.util.Optional;

public interface OutboxRepository {

    void save(TransactionOutbox outbox);
    Optional<TransactionOutbox> findByTransactionId(String transactionId);
    List<TransactionOutbox> findBySentFalseOrderByCreatedAtAsc();
}
