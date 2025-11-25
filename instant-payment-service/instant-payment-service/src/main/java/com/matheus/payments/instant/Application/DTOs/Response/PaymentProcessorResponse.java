package com.matheus.payments.instant.Application.DTOs.Response;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PaymentProcessorResponse {

    private UUID transactionId;
    private UUID senderAccountId;
    private UUID receiverAccountId;
    private Boolean isSent;
    private Boolean isSucessful;
    private Boolean isFailed;
    private String failedMessage;

    public PaymentProcessorResponse() {
    }

    public static PaymentProcessorResponse failedResponse(UUID transactionId, UUID senderAccountId, UUID receiverAccountId, String failedMessage) {

        PaymentProcessorResponse response = new PaymentProcessorResponse();
        response.transactionId = transactionId;
        response.isSent = true;
        response.isFailed = true;
        response.isSucessful = false;
        response.senderAccountId = senderAccountId;
        response.receiverAccountId = receiverAccountId;
        response.failedMessage = failedMessage;
        return response;
    }

    public static PaymentProcessorResponse successResponse(UUID transactionId, UUID senderAccountId, UUID receiverAccountId) {

        PaymentProcessorResponse response = new PaymentProcessorResponse();
        response.transactionId = transactionId;
        response.isSent = true;
        response.isFailed = false;
        response.isSucessful = true;
        response.senderAccountId = senderAccountId;
        response.receiverAccountId = receiverAccountId;
        response.failedMessage = null;
        return response;
    }

    public static PaymentProcessorResponse connectionFailed(UUID transactionId) {

        PaymentProcessorResponse response = new PaymentProcessorResponse();
        response.transactionId = transactionId;
        response.isSent = true;
        response.isFailed = true;
        response.isSucessful = false;
        response.senderAccountId = null;
        response.receiverAccountId = null;
        response.failedMessage = "Error sending payment to processor occurred while trying to reach Wallet Server. The payment could not be processed!";
        return response;
    }
}
