package com.matheus.payments.instant.Application.DTOs.Request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TransactionRequest {

    private String transactionId;
    @NotNull(message = "Sender value can't be Null") @NotEmpty(message = "Sender value can't be Empty")
    private String senderKey;
    @NotNull(message = "Receiver value can't be Null") @NotEmpty(message = "Receiver value can't be Empty")
    private String receiverKey;
    @NotNull(message = "Amount value can't be Null") @NotEmpty(message = "Amount value can't be Empty")
    private BigDecimal amount;

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}


