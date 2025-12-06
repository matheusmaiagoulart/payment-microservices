package com.matheus.payments.wallet.Infra.Repository;

import com.matheus.payments.wallet.Domain.TransactionsProcessed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionProcessedRepository extends JpaRepository<TransactionsProcessed, UUID> {

}
