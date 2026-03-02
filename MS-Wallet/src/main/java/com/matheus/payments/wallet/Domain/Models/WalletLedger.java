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
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private WalletEntryType entryType;

    private LocalDateTime timestamp;

    public WalletLedger() {}

    public WalletLedger(UUID transactionId, UUID walletId, UUID counterpartyWalletId, BigDecimal amount, TransactionType transactionType, WalletEntryType entryType, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.transactionType = transactionType;
        this.counterpartyWalletId = counterpartyWalletId;
        this.amount = amount;
        this.entryType = entryType;
        this.timestamp = timestamp;
    }

    public WalletLedger createDebitEntry(String transactionId, UUID walletId, UUID counterpartyWalletId, BigDecimal amount) {
        return new WalletLedger(UUID.fromString(transactionId), walletId, counterpartyWalletId, amount, TransactionType.INSTANT_PAYMENT, WalletEntryType.DEBIT, LocalDateTime.now());
    }

    public WalletLedger createCreditEntry(String transactionId, UUID walletId, UUID counterpartyWalletId, BigDecimal amount) {
        return new WalletLedger(UUID.fromString(transactionId), walletId, counterpartyWalletId, amount, TransactionType.INSTANT_PAYMENT, WalletEntryType.CREDIT, LocalDateTime.now());
    }

    public WalletLedger createDepositEntry(String transactionId, UUID walletId, BigDecimal amount) {
        return new WalletLedger(UUID.fromString(transactionId), walletId, walletId, amount, TransactionType.DEPOSIT, WalletEntryType.CREDIT, LocalDateTime.now());
    }

    public enum WalletEntryType {
        DEBIT,
        CREDIT
    }

    public enum TransactionType {
        INSTANT_PAYMENT,
        DEPOSIT,
    }
}
