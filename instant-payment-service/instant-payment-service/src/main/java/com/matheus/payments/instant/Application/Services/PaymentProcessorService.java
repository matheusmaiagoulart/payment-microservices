package com.matheus.payments.instant.Application.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.instant.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.instant.Domain.Transaction;
import com.matheus.payments.instant.Domain.TransactionOutbox;
import com.matheus.payments.instant.Domain.TransactionStatus;
import com.matheus.payments.instant.Infra.Exceptions.Custom.FailedToSentException;
import com.matheus.payments.instant.Infra.Exceptions.Custom.TransactionAlreadySentException;
import com.matheus.payments.instant.Infra.Exceptions.Custom.TransactionFailedException;
import com.matheus.payments.instant.Infra.Exceptions.Custom.TransactionNotFound;
import com.matheus.payments.instant.Infra.Http.WalletServer;
import com.matheus.payments.instant.Infra.Repository.OutboxRepository;
import com.matheus.payments.instant.Infra.Repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
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
            log.info("Sending payment to Wallet Server",
                    LogBuilder.requestLog("POST", "/wallets/instant-payment", "Wallet", transactionId, "PaymentProcessorService", "sendPaymentToProcessor",
                            kv("event", "payment.request.sending")));

            response = sendToWalletServer(payloadJson); // Send instant-payment request to Wallet Server

        } catch (FailedToSentException e) {

            log.warn("Error to sent a request for Wallet Server",
                    LogBuilder.requestLog("POST", "/wallets/instant-payment", "Wallet", transactionId, "PaymentProcessorService", "sendPaymentToProcessor",
                            kv("event", "payment.request.send.failed"),
                            kv("errorMessage", e.getMessage())));

            PaymentProcessorResponse failedResponse = PaymentProcessorResponse.connectionFailed(UUID.fromString(transactionId));
            paymentStatusUpdate(failedResponse);
            throw e;
        }

        log.info("Successfully sent payment to Wallet Server",
                LogBuilder.requestLog("POST", "/wallets/instant-payment", "Wallet", transactionId, "PaymentProcessorService", "sendPaymentToProcessor",
                        kv("event", "payment.request.sent.success")));

        outbox.setSent(true);
        outboxRepository.save(outbox);
        return response;
    }


    public PaymentProcessorResponse paymentStatusUpdate(PaymentProcessorResponse response) {


        log.info("Payment response received from Wallet Server",
                LogBuilder.requestLog("POST", "/wallets/instant-payment", "Wallet", response.getTransactionId().toString(), "PaymentProcessorService", "paymentStatusUpdate",
                kv("event", "payment.response.received")));


        Transaction transaction = getTransactionById(response.getTransactionId());

        if (!response.getIsSuccessful()) {


            log.warn("Payment response for payment failed from Wallet Server",
                    LogBuilder.requestLog("POST", "/wallets/instant-payment", "Wallet", response.getTransactionId().toString(), "PaymentProcessorService", "paymentStatusUpdate",
                            kv("event", "payment.response.failed"),
                            kv("failureReason", response.getFailedMessage())));

            TransactionOutbox outbox = getOutboxByTransactionId(response.getTransactionId().toString());

            outbox.setFailed(true);
            outbox.setFailureReason(response.getFailedMessage());
            outbox.setFailureAt(LocalDateTime.now());
            outboxRepository.save(outbox);

            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            throw new TransactionFailedException(response.getFailedMessage());
        }

        log.info("Payment response received from Wallet Server was successfully",
                LogBuilder.requestLog("POST", "/wallets/instant-payment", "Wallet", response.getTransactionId().toString(), "PaymentProcessorService", "paymentStatusUpdate",
                        kv("event", "wallet.response.received.success")));

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setSenderAccountId(response.getSenderAccountId());
        transaction.setReceiverAccountId(response.getReceiverAccountId());
        transactionRepository.save(transaction);
        return response;
    }

    private Transaction getTransactionById(UUID transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFound("Transaction with ID " + transactionId + " not found."));
    }

    private TransactionOutbox getOutboxByTransactionId(String transactionId) {
        return outboxRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFound("Transaction with ID " + transactionId + " not found."));
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
            throw new FailedToSentException("Error sending payment to processor occurred while trying to reach Wallet Server. The payment could not be processed!");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FailedToSentException("An error occurred while we processing your payment request. Please try again later!");
        }
    }
}

