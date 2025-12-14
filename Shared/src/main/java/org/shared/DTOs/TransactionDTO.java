package org.shared.DTOs;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionDTO {

    private String transactionId;
    private String senderKey;
    private String receiverKey;
    private UUID senderAccountId;
    private UUID receiverAccountId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime timestamp;

    public TransactionDTO() {}

        public TransactionDTO(UUID transactionId, String senderKey, String receiverKey,UUID senderAccountId, UUID receiverAccountId, BigDecimal amount, String status, LocalDateTime timestamp) {
        this.transactionId = transactionId.toString();
        this.senderKey = senderKey;
        this.receiverKey = receiverKey;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getSenderKey() {
        return senderKey;
    }

    public String getReceiverKey() {
        return receiverKey;
    }

    public UUID getSenderAccountId() {
        return senderAccountId;
    }

    public UUID getReceiverAccountId() {
        return receiverAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }
}
