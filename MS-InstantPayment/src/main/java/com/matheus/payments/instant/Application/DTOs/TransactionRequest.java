package com.matheus.payments.instant.Application.DTOs;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.math.BigDecimal;

@Getter
public class TransactionRequest {
    @Setter
    private String transactionId;

    @NotBlank(message = "Sender value can't be Null or empty")
    private String senderKey;
    @NotBlank(message = "Receiver value can't be Null or empty")
    private String receiverKey;

    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @NotNull(message = "Amount value can't be Null")
    private BigDecimal amount;
}


