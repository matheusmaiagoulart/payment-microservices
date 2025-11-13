package com.matheus.payments.wallet.Application.DTOs.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class PaymentProcessorResponse {

    private UUID TransactionId;
    private Boolean isSucessful;
    private Boolean isFailed;
    private String failedMessage;

    public PaymentProcessorResponse(UUID transactionId, Boolean isSucessful, Boolean isFailed, String failedMessage) {
        TransactionId = transactionId;
        this.isSucessful = isSucessful;
        this.isFailed = isFailed;
        this.failedMessage = failedMessage;
    }


}
