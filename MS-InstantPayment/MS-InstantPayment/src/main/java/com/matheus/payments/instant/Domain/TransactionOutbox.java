package com.matheus.payments.instant.Domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a Transaction Outbox Pattern entry for reliable message delivery.
 * <p>
 * This class manages details related to Transaction and stores related data about the Transaction processing.
 * <p>
 *  It is used to manage delivery status, failure reasons and ensure consistency in the Transaction processing workflow.
 */
@Getter
@Setter
@Document(collection = "transaction_outbox")
public class TransactionOutbox {

    @Id
    private String transactionId;
    private String payload;
    private Boolean sent;
    private Boolean failed;
    private LocalDateTime createdAt;
    private String failureReason;
    private LocalDateTime failureAt;

    public TransactionOutbox(String transactionId, String payload) {
        this.transactionId = transactionId;
        this.payload = payload;
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
