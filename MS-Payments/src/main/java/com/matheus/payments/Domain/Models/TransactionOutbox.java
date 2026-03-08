package com.matheus.payments.Domain.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a Transaction Outbox Pattern entry for reliable message delivery.
 * <p>
 * This class manages details related to Transaction and stores related data about the Transaction processing.
 * <p>
 *  It is used to manage delivery status, failure reasons and ensure consistency in the Transaction processing workflow.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "transaction_outbox")
public class TransactionOutbox {

    @Id
    private String transactionId;
    private String topic;
    private String payload;
    private UUID correlationId;
    private Boolean sent;
    private Boolean failed;
    private LocalDateTime createdAt;
    private String failureReason;
    private LocalDateTime failureAt;

    public TransactionOutbox(String transactionId, String payload, String correlationId, String topic) {
        this.transactionId = transactionId;
        this.topic = topic;
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
