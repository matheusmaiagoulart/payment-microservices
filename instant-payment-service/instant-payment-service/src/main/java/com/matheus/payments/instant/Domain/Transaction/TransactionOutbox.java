package com.matheus.payments.instant.Domain.Transaction;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

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
}
