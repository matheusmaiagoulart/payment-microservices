package com.matheus.payments.Domain.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Entity
@Table(name = "transaction_idempotency")
@NoArgsConstructor
public class TransactionIdempotency {

    @Id
    private UUID transactionId;
    private String payload;
    private UUID correlationId;
    private Boolean sent;
    private Boolean failed;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime failureAt;

    public TransactionIdempotency(UUID transactionId, String payload, String correlationId) {
        this.transactionId = transactionId;
        this.payload = payload;
        this.correlationId = UUID.fromString(correlationId);
        this.sent = false;
        this.failed = false;
        this.createdAt = LocalDateTime.now();
        this.failureReason = null;
        this.failureAt = null;
    }

    public void failedTransaction(String failedReason){
        this.failed = true;
        this.failureReason = failedReason;
        this.failureAt = LocalDateTime.now();
    }
}
