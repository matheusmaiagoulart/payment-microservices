package com.matheus.payments.wallet.Infra.Repository.JpaInterfaces;

import com.matheus.payments.wallet.Domain.Models.TransactionsProcessed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaTransactionProcessedRepository extends JpaRepository<TransactionsProcessed, UUID> {

    boolean existsByTransactionId(UUID transactionId);
}

