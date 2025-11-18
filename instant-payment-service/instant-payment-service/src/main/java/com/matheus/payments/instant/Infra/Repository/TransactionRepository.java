package com.matheus.payments.instant.Infra.Repository;

import com.matheus.payments.instant.Domain.Transaction.Transaction;
import com.matheus.payments.instant.Domain.Transaction.TransactionOutbox;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    Optional<Transaction> findByTransactionId(UUID transactionId);

}
