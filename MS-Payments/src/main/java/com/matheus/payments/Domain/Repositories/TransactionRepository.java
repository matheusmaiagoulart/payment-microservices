package com.matheus.payments.Domain.Repositories;

import com.matheus.payments.Domain.Models.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository {

    void save(Transaction transaction);
    Optional<Transaction> findByTransactionId(UUID transactionId);
    Optional<List<Transaction>> findTransactionsBySenderAccountIdOrReceiverAccountId(UUID senderAccountId, UUID receiverAccountId);
}
