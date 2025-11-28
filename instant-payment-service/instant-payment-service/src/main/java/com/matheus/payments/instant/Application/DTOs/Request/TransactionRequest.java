package com.matheus.payments.instant.Application.DTOs.Request;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TransactionRequest {

    private String senderKey;
    private String receiverKey;
    private BigDecimal amount;
}
