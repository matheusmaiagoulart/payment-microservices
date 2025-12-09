package com.matheus.payments.instant.Application.DTOs.Response;

import lombok.Getter;
import java.util.UUID;

@Getter
public class PaymentProcessorResponse {

    private UUID transactionId;
    private UUID senderAccountId;
    private UUID receiverAccountId;
    private Boolean isSent;
    private Boolean isSuccessful;
    private Boolean isFailed;
    private String failedMessage;

    public PaymentProcessorResponse() {}

    public static PaymentProcessorResponse connectionFailed(UUID transactionId)
    {
        PaymentProcessorResponse response = new PaymentProcessorResponse();
        response.transactionId = transactionId;
        response.isSent = false;
        response.isFailed = true;
        response.isSuccessful = false;
        response.senderAccountId = null;
        response.receiverAccountId = null;
        response.failedMessage = "Error sending payment to processor occurred while trying to reach Wallet Server. The payment could not be processed!";
        return response;
    }
}
