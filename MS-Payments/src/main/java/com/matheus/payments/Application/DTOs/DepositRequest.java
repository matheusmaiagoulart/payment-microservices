package com.matheus.payments.Application.DTOs;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class DepositRequest {

    @NotNull(message = "Receiver Account ID cannot be null")
    public UUID receiverId;

    @NotNull(message = "Amount cannot be blank")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    public BigDecimal amount;
}
