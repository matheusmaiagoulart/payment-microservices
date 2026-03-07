package com.matheus.payments.Infra.Repository.JpaInterfaces;

import com.matheus.payments.Domain.Models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaTransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByTransactionId(UUID transactionId);

    // Get all transaction from User by accountId (either sender or receiver)
    Optional<List<Transaction>> findTransactionsBySenderAccountIdOrReceiverAccountId(UUID senderAccountId, UUID receiverAccountId);

}
