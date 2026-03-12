package com.matheus.payments.wallet.Domain.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 *
 * Entity to keep track of processed transactions to ensure idempotency.
 *
 * @author Matheus Maia Goulart
 */

@Getter
@Entity
@NoArgsConstructor
@Table(name = "transactions_processed")
public class TransactionsProcessed {

    @Id
    private UUID transactionId;

    private LocalDateTime timestamp;

    public TransactionsProcessed(UUID transactionId) {
        this.transactionId = transactionId;
        this.timestamp = LocalDateTime.now();
    }
}
