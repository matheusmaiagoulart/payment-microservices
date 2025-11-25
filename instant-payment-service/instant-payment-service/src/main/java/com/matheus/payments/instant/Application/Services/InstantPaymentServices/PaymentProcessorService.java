package com.matheus.payments.instant.Application.Services.InstantPaymentServices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.instant.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.instant.Domain.Transaction.Transaction;
import com.matheus.payments.instant.Domain.Transaction.TransactionOutbox;
import com.matheus.payments.instant.Domain.Transaction.TransactionStatus;
import com.matheus.payments.instant.Infra.Exceptions.Custom.FailedToSentException;
import com.matheus.payments.instant.Infra.Exceptions.Custom.TransactionAlreadySentException;
import com.matheus.payments.instant.Infra.Exceptions.Custom.TransactionFailedException;
import com.matheus.payments.instant.Infra.Exceptions.Custom.TransactionNotFound;
import com.matheus.payments.instant.Infra.Http.Clients.WalletServer;
import com.matheus.payments.instant.Infra.Repository.OutboxRepository;
import com.matheus.payments.instant.Infra.Repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentProcessorService {

    private final OutboxRepository outboxRepository;
    private final WalletServer walletServerRequest;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    public PaymentProcessorService(OutboxRepository outboxRepository, WalletServer walletServerRequest, ObjectMapper objectMapper, TransactionRepository transactionRepository) {
        this.outboxRepository = outboxRepository;
        this.walletServerRequest = walletServerRequest;
        this.objectMapper = objectMapper;
        this.transactionRepository = transactionRepository;
    }

    public String sendPaymentToProcessor(String transactionId) throws IOException {

        var outbox = getOutboxByTransactionId(transactionId);
        ensureNotAlreadySent(outbox);

        String payloadJson = outbox.getPayload();
        String response;

        try {
            response = sendToWalletServer(payloadJson);

        } catch (FailedToSentException e) {

            PaymentProcessorResponse failedResponse = PaymentProcessorResponse.connectionFailed(UUID.fromString(transactionId));
            String failedResponseJson = objectMapper.writeValueAsString(failedResponse);
            paymentStatusUpdate(failedResponseJson);
            throw e;
        }
        outbox.setSent(true);
        outboxRepository.save(outbox);
        return response;
    }


    public PaymentProcessorResponse paymentStatusUpdate(String message) throws JsonProcessingException {

        PaymentProcessorResponse response = objectMapper.readValue(message, PaymentProcessorResponse.class);

        System.out.println("Payment status update received: " + response);

        Transaction transaction = transactionRepository.findByTransactionId(response.getTransactionId()).orElseThrow(() -> new TransactionNotFound("Transaction with ID " + response.getTransactionId() + " not found."));

        if (!response.getIsSucessful()) {
            TransactionOutbox outbox = outboxRepository.findByTransactionId(response.getTransactionId().toString()).orElseThrow(() -> new TransactionNotFound("Outbox entry for Transaction ID " + response.getTransactionId() + " not found."));

            outbox.setFailed(true);
            outbox.setFailureReason(response.getFailedMessage());
            outbox.setFailureAt(LocalDateTime.now());
            outboxRepository.save(outbox);

            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            throw new TransactionFailedException(response.getFailedMessage());
        }

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setSenderAccountId(response.getSenderAccountId());
        transaction.setReceiverAccountId(response.getReceiverAccountId());
        transactionRepository.save(transaction);
        return response;
    }

    // Auxiliar methods

    private TransactionOutbox getOutboxByTransactionId(String transactionId) {
        return outboxRepository.findByTransactionId(transactionId).orElseThrow(() -> new TransactionNotFound("Transaction with ID " + transactionId + " not found."));
    }

    private void ensureNotAlreadySent(TransactionOutbox transaction) {
        if (transaction.getSent()) {
            throw new TransactionAlreadySentException("Transaction with ID " + transaction.getTransactionId() + " has already been sent.");
        }
    }

    private String sendToWalletServer(String payloadJson) {
        try {
            HttpResponse<String> response = walletServerRequest.instantPaymentRequest(payloadJson);
            return response.body();

        } catch (IOException e) {
            System.out.println("Error sending payment to processor error occurred while trying to reach Wallet Server. The payment could not be processed: " + e.getMessage());
            throw new FailedToSentException("Error sending payment to processor occurred while trying to reach Wallet Server. The payment could not be processed!");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("An error occurred while we processing your payment request. Please try again later! " + e.getMessage());
            throw new FailedToSentException("An error occurred while we processing your payment request. Please try again later!");
        }
    }
}

