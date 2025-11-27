package com.matheus.payments.instant.Application.DTOs.Response;

import com.matheus.payments.instant.Domain.TransactionStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TransactionDTO {

    private String transactionId;
    private String senderKey;
    private String receiverKey;
    private UUID senderAccountId;
    private UUID receiverAccountId;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime timestamp;

    public TransactionDTO(UUID transactionId, String senderKey, String receiverKey,UUID senderAccountId, UUID receiverAccountId, BigDecimal amount, TransactionStatus status, LocalDateTime timestamp) {
        this.transactionId = transactionId.toString();
        this.senderKey = senderKey;
        this.receiverKey = receiverKey;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

}
