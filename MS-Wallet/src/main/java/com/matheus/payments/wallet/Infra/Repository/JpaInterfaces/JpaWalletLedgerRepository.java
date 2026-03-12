package com.matheus.payments.wallet.Infra.Repository.JpaInterfaces;

import com.matheus.payments.wallet.Domain.Models.WalletLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaWalletLedgerRepository extends JpaRepository<WalletLedger, UUID> {

    List<WalletLedger> findByTransactionId(UUID transactionId);
}

