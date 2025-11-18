package com.matheus.payments.instant.Application.DTOs.Response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class PaymentProcessorResponse {


    private UUID TransactionId;
    private Boolean isSent;
    private Boolean isSucessful;
    private Boolean isFailed;
    private String failedMessage;

    public PaymentProcessorResponse() {
    }
    public PaymentProcessorResponse(UUID transactionId, Boolean isSent, Boolean isSucessful, Boolean isFailed, String failedMessage) {
        this.TransactionId = transactionId;
        this.isSent = isSent;
        this.isSucessful = isSucessful;
        this.isFailed = isFailed;
        this.failedMessage = failedMessage;
    }
}
