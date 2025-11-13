package com.matheus.payments.instant.Application.DTOs;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TransactionDTO {

    private String transactionId;
    private String senderId;
    private String receiverId;
    private BigDecimal amount;
    private Boolean processed;
    private LocalDateTime timestamp;

    public TransactionDTO(UUID transactionId, UUID senderId, UUID receiverId, BigDecimal amount, Boolean processed, LocalDateTime timestamp) {
        this.transactionId = transactionId.toString();
        this.senderId = senderId.toString();
        this.receiverId = receiverId.toString();
        this.amount = amount;
        this.processed = processed;
        this.timestamp = timestamp;
    }

}
