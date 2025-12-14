package org.shared.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;


public class PaymentProcessorResponse {

    private UUID transactionId;
    private UUID senderAccountId;
    private UUID receiverAccountId;
    private boolean isSent;
    private boolean isSuccessful;
    private boolean isFailed;
    private LocalDateTime timestamp;

    @JsonProperty("alreadyProcessed")
    private boolean isAlreadyProcessed;
    private String failedMessage;

    public PaymentProcessorResponse() {}

    public static PaymentProcessorResponse failedResponse(boolean isAlreadyProcessed, UUID transactionId, UUID senderAccountId, UUID receiverAccountId, String failedMessage) {

        PaymentProcessorResponse response = new PaymentProcessorResponse();
        response.isAlreadyProcessed = isAlreadyProcessed;
        response.transactionId = transactionId;
        response.isSent = true;
        response.isFailed = true;
        response.isSuccessful = false;
        response.senderAccountId = senderAccountId;
        response.receiverAccountId = receiverAccountId;
        response.failedMessage = failedMessage;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    public static PaymentProcessorResponse successResponse(UUID transactionId, UUID senderAccountId, UUID receiverAccountId) {

        PaymentProcessorResponse response = new PaymentProcessorResponse();
        response.transactionId = transactionId;
        response.isSent = true;
        response.isFailed = false;
        response.isSuccessful = true;
        response.senderAccountId = senderAccountId;
        response.receiverAccountId = receiverAccountId;
        response.failedMessage = null;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    public static PaymentProcessorResponse connectionFailed(UUID transactionId) {

        PaymentProcessorResponse response = new PaymentProcessorResponse();
        response.transactionId = transactionId;
        response.isSent = true;
        response.isFailed = true;
        response.isSuccessful = false;
        response.senderAccountId = null;
        response.receiverAccountId = null;
        response.timestamp = LocalDateTime.now();
        response.failedMessage = "Error sending payment to processor occurred while trying to reach Wallet Server. The payment could not be processed!";
        return response;
    }

    public static PaymentProcessorResponse responseAlreadyProcessed(UUID transactionId, UUID senderAccountId, UUID receiverAccountId, LocalDateTime timestamp) {

        PaymentProcessorResponse response = new PaymentProcessorResponse();
        response.transactionId = transactionId;
        response.isAlreadyProcessed = true;
        response.timestamp = timestamp;
        response.isSent = true;
        response.isFailed = false;
        response.isSuccessful = true;
        response.senderAccountId = senderAccountId;
        response.receiverAccountId = receiverAccountId;
        response.failedMessage = null;
        return response;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getSenderAccountId() {
        return senderAccountId;
    }

    public UUID getReceiverAccountId() {
        return receiverAccountId;
    }

    public Boolean getIsSent() {
        return isSent;
    }

    public Boolean getIsSuccessful() {
        return isSuccessful;
    }

    public Boolean getIsFailed() {
        return isFailed;
    }

    public String getFailedMessage() {
        return failedMessage;
    }

    public boolean isAlreadyProcessed() {
        return isAlreadyProcessed;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
