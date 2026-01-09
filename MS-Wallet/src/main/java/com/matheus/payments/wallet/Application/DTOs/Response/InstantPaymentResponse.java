package com.matheus.payments.wallet.Application.DTOs.Response;

import lombok.Getter;

import java.util.UUID;

@Getter
public class InstantPaymentResponse {

    private boolean isSucessful;
    private boolean alreadyProcessed;
    private UUID senderAccountId;
    private UUID receiverAccountId;
    private String failedMessage;

    public InstantPaymentResponse(boolean isSuccessful, boolean alreadyProcessed, UUID senderAccountId, UUID receiverAccountId, String failedMessage) {
        this.isSucessful = isSuccessful;
        this.alreadyProcessed = alreadyProcessed;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.failedMessage = failedMessage ;
    }

    public InstantPaymentResponse(boolean isSuccessful, boolean alreadyProcessed, UUID senderAccountId, UUID receiverAccountId) {
        this.isSucessful = isSuccessful;
        this.alreadyProcessed = alreadyProcessed;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.failedMessage = null;
    }
}
