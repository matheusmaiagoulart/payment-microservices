package com.matheus.payments.instant.Infra.Repository;

import com.matheus.payments.instant.Domain.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    Optional<Transaction> findByTransactionId(UUID transactionId);

    // Get all transaction from User by accountId (either sender or receiver)
    Optional<List<Transaction>> findTransactionsBySenderAccountIdOrReceiverAccountId(UUID senderAccountId, UUID receiverAccountId);

}
