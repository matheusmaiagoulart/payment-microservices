package com.matheus.payments.instant.Infra.Repository;

import com.matheus.payments.instant.Application.DTOs.TransactionDTO;
import com.matheus.payments.instant.Domain.transaction.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    Optional<Transaction> findByTransactionId(String transactionId);
}
