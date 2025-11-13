package com.matheus.payments.instant.Infra.Repository;

import com.matheus.payments.instant.Domain.transaction.TransactionOutbox;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OutboxRepository extends MongoRepository<TransactionOutbox, String> {

    Optional<TransactionOutbox> findByTransactionId(String transactionId);

}
