package com.matheus.payments.Infra.Repository;

import com.matheus.payments.Domain.TransactionOutbox;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OutboxRepository extends MongoRepository<TransactionOutbox, String> {

    Optional<TransactionOutbox> findByTransactionId(String transactionId);

}
