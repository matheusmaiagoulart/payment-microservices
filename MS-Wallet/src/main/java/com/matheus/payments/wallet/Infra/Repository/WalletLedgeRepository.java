package com.matheus.payments.wallet.Infra.Repository;

import com.matheus.payments.wallet.Domain.Models.WalletLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletLedgeRepository extends JpaRepository<WalletLedger, UUID> {

    List<WalletLedger> findByTransactionId(UUID transactionId);
}
