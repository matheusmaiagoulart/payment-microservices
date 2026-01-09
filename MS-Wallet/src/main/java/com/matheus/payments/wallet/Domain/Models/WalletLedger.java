package com.matheus.payments.wallet.Domain.Models;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 *
 * Entity to keep track of processed transactions to audit of wallet operations involved with transfer process.
 *
 * @author Matheus Maia Goulart
 */

@Getter
@Entity
@Table(name = "wallet_ledger")
public class WalletLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID transactionId;

    private UUID walletId;

    private UUID counterpartyWalletId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private EntryType entryType;

    private LocalDateTime timestamp;

    public WalletLedger() {}

    public WalletLedger(UUID transactionId, UUID walletId, UUID counterpartyWalletId, BigDecimal amount, EntryType entryType, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.counterpartyWalletId = counterpartyWalletId;
        this.amount = amount;
        this.entryType = entryType;
        this.timestamp = timestamp;
    }

    public WalletLedger createDebitEntry(String transactionId, UUID walletId, UUID counterpartyWalletId, BigDecimal amount) {
        return new WalletLedger(UUID.fromString(transactionId), walletId, counterpartyWalletId, amount, EntryType.DEBIT, LocalDateTime.now());
    }

    public WalletLedger createCreditEntry(String transactionId, UUID walletId, UUID counterpartyWalletId, BigDecimal amount) {
        return new WalletLedger(UUID.fromString(transactionId), walletId, counterpartyWalletId, amount, EntryType.CREDIT, LocalDateTime.now());
    }

    enum EntryType {
        DEBIT,
        CREDIT
    }
}
