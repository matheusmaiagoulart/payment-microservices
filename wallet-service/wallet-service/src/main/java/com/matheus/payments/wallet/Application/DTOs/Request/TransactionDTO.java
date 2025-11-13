package com.matheus.payments.wallet.Application.DTOs.Request;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TransactionDTO {

    private UUID transactionId;
    private UUID senderId;
    private UUID receiverId;
    private BigDecimal amount;
    private Boolean processed;
    private LocalDateTime timestamp;

    public TransactionDTO(){}


//    public TransactionDTO(String transactionId, String senderId, String receiverId, BigDecimal amount, Boolean processed, LocalDateTime timestamp) {
//        this.transactionId = UUID.fromString(transactionId);
//        this.senderId = UUID.fromString(senderId);
//        this.receiverId = UUID.fromString(receiverId);
//        this.amount = amount;
//        this.processed = processed;
//        this.timestamp = timestamp;
//    }
}
