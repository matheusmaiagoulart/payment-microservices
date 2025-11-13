package com.matheus.payments.instant.Application.DTOs;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class TransactionRequest {

    private UUID senderId;
    private UUID receiverId;
    private BigDecimal amount;
}
