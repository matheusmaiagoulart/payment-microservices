package com.matheus.payments.Application.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.Application.Audit.PaymentProcessorAudit;
import com.matheus.payments.Domain.Models.Transaction;
import com.matheus.payments.Domain.Models.TransactionIdempotency;
import com.matheus.payments.Infra.Exceptions.Custom.FailedToSentException;
import com.matheus.payments.Infra.Exceptions.Custom.TransactionAlreadySentException;
import com.matheus.payments.Domain.Exceptions.TransactionFailedException;
import com.matheus.payments.Infra.Http.WalletService;
import io.github.resilience4j.retry.annotation.Retry;
import org.shared.DTOs.PaymentProcessorResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
public class PaymentProcessorService {

    private final PaymentProcessorAudit audit;
    private final ObjectMapper mapper;
    private final TransactionIdempotencyService idempotencyService;
    private final WalletService walletServerRequest;
    private final TransactionService transactionService;

    public PaymentProcessorService(TransactionIdempotencyService idempotencyService, ObjectMapper mapper, WalletService walletServerRequest, TransactionService transactionService, PaymentProcessorAudit audit) {
        this.audit = audit;
        this.mapper = mapper;
        this.idempotencyService = idempotencyService;
        this.transactionService = transactionService;
        this.walletServerRequest = walletServerRequest;
    }


    public PaymentProcessorResponse sendPaymentToProcessor(String transactionId) {
        try {
            TransactionIdempotency transactionIdempotency = idempotencyService.getByTransactionId(UUID.fromString(transactionId));
            ensureNotAlreadySent(transactionIdempotency);
            String payloadJson = transactionIdempotency.getPayload();
            PaymentProcessorResponse response = executeAndParseRequest(payloadJson, transactionId);

            audit.logSentSuccessfullyWallet(transactionId); // LOG
            idempotencyService.setTransactionSent(transactionIdempotency);
            return response;
        } catch (FailedToSentException e) {
            audit.logErrorSendingRequestWallet(transactionId, e); // LOG
            PaymentProcessorResponse failedResponse = PaymentProcessorResponse.connectionFailed(UUID.fromString(transactionId));
            paymentStatusUpdate(failedResponse);
            throw e;
        }
    }

    private PaymentProcessorResponse executeAndParseRequest(String payloadJson, String transactionId) throws FailedToSentException {
        audit.logSendingRequestWallet(transactionId);
        String walletResponse = sendToWalletServer(payloadJson);
        try {
            return mapper.readValue(walletResponse, PaymentProcessorResponse.class);
        }
        catch (JsonProcessingException e) {
            audit.logErrorToParseResponseInformations(transactionId, walletResponse, e.getMessage()); // LOG
            throw new FailedToSentException("An error occurred while processing the response from the payment processor. Please try again later!");
        }
    }

    @Retry(name = "databaseRetry")
    @Transactional
    public PaymentProcessorResponse paymentStatusUpdate(PaymentProcessorResponse response) {
        Transaction transaction = transactionService.getTransactionById(response.getTransactionId());
        TransactionIdempotency transactionIdempotency = idempotencyService.getByTransactionId(response.getTransactionId());

        if (!response.getIsSent()) {
            return handleFailed(response, transaction, transactionIdempotency);
        }
        if (response.isAlreadyProcessed()) {
            return handleAlreadyProcessed(transaction);
        }
        if (!response.getIsSuccessful()) {
            return handleFailed(response, transaction, transactionIdempotency);
        }

        audit.logReceivedSuccessResponse(transaction.getTransactionId().toString()); // LOG
        return handleSuccess(response, transaction);
    }

    private void ensureNotAlreadySent(TransactionIdempotency transaction) throws TransactionAlreadySentException {
        if (transaction.getSent()) {
            throw new TransactionAlreadySentException("Transaction with ID " + transaction.getTransactionId() + " has already been sent.");
        }
    }

    private String sendToWalletServer(String payloadJson) throws FailedToSentException {
        try {
            HttpResponse<String> response = walletServerRequest.instantPaymentRequest(payloadJson);
            return response.body();
        } catch (IOException | TimeoutException e) {
            throw new FailedToSentException("Error sending payment to processor occurred while trying to reach Wallet Server. The payment could not be processed!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FailedToSentException("An error occurred while we processing your payment request. Please try again later!");
        }
    }

    @Retry(name = "databaseRetry")
    @Transactional
    public void handleFailedTransaction(String failedMessage, Transaction transaction, TransactionIdempotency transactionIdempotency) {
        transactionIdempotency.failedTransaction(failedMessage);
        idempotencyService.save(transactionIdempotency);

        transaction.setTransactionFailed();
        transactionService.save(transaction);
    }

    private PaymentProcessorResponse handleFailed(PaymentProcessorResponse response, Transaction transaction, TransactionIdempotency idempotency) {
        audit.logReceiveResponse(response.getTransactionId().toString()); // LOG
        audit.logReceivedFailedResponse(transaction.getTransactionId().toString(), response.getFailedMessage()); // LOG
        handleFailedTransaction(response.getFailedMessage(), transaction, idempotency);
        throw new TransactionFailedException(response.getFailedMessage());
    }

    private PaymentProcessorResponse handleSuccess(PaymentProcessorResponse response, Transaction transaction) {
        audit.logReceiveResponse(response.getTransactionId().toString()); // LOG
        transaction.setTransactionCompleted(response.getSenderAccountId(), response.getReceiverAccountId());
        transactionService.save(transaction);
        return response;
    }

    private PaymentProcessorResponse handleAlreadyProcessed(Transaction transaction) {
        audit.logReceivedSuccessResponse(transaction.getTransactionId().toString());
        return PaymentProcessorResponse.responseAlreadyProcessed(
                transaction.getTransactionId(),
                transaction.getSenderAccountId(),
                transaction.getReceiverAccountId(),
                transaction.getTimestamp());

    }
}

