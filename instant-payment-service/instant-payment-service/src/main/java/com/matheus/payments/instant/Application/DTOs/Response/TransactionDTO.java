package com.matheus.payments.instant.Application.DTOs.Response;

import com.matheus.payments.instant.Domain.Transaction.TransactionStatus;
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
    private TransactionStatus status;
    private LocalDateTime timestamp;

    public TransactionDTO(UUID transactionId, UUID senderId, UUID receiverId, BigDecimal amount, TransactionStatus status, LocalDateTime timestamp) {
        this.transactionId = transactionId.toString();
        this.senderId = senderId.toString();
        this.receiverId = receiverId.toString();
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

}
