package com.matheus.payments.instant.Application.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.instant.Application.Audit.PaymentProcessorAudit;
import com.matheus.payments.instant.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.instant.Domain.Transaction;
import com.matheus.payments.instant.Domain.TransactionOutbox;
import com.matheus.payments.instant.Domain.TransactionStatus;
import com.matheus.payments.instant.Infra.Exceptions.Custom.*;
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
import java.util.concurrent.TimeoutException;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class PaymentProcessorService {

    private final PaymentProcessorAudit audit;
    private final OutboxRepository outboxRepository;
    private final WalletServer walletServerRequest;
    private final TransactionRepository transactionRepository;

    public PaymentProcessorService(OutboxRepository outboxRepository, WalletServer walletServerRequest, TransactionRepository transactionRepository, PaymentProcessorAudit audit) {
        this.audit = audit;
        this.outboxRepository = outboxRepository;
        this.walletServerRequest = walletServerRequest;
        this.transactionRepository = transactionRepository;
    }

    public String sendPaymentToProcessor(String transactionId) {

        var outbox = getOutboxByTransactionId(transactionId);
        ensureNotAlreadySent(outbox);

        String payloadJson = outbox.getPayload();
        String response;
        try {
            audit.logSendingRequestWallet(transactionId); // LOG
            response = sendToWalletServer(payloadJson);

        } catch (FailedToSentException e)
        {
            audit.logErrorSendingRequestWallet(transactionId, e);

            PaymentProcessorResponse failedResponse = PaymentProcessorResponse.connectionFailed(UUID.fromString(transactionId));
            paymentStatusUpdate(failedResponse);
            throw e;
        }

        audit.logSentSuccessfullyWallet(transactionId); // LOG

        outbox.setSent(true);
        outboxRepository.save(outbox);
        return response;
    }

    public PaymentProcessorResponse paymentStatusUpdate(PaymentProcessorResponse response) {

        audit.logReceiveResponse(response.getTransactionId().toString()); // LOG

        Transaction transaction = getTransactionById(response.getTransactionId());

        if (!response.getIsSuccessful())
        {
            audit.logReceivedFailedResponse(transaction.getTransactionId().toString(), response.getFailedMessage()); // LOG

            TransactionOutbox outbox = getOutboxByTransactionId(response.getTransactionId().toString());

            outbox.setFailed(true);
            outbox.setFailureReason(response.getFailedMessage());
            outbox.setFailureAt(LocalDateTime.now());
            outboxRepository.save(outbox);

            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            throw new TransactionFailedException(response.getFailedMessage());
        }

        audit.logReceivedSuccessResponse(transaction.getTransactionId().toString()); // LOG

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setSenderAccountId(response.getSenderAccountId());
        transaction.setReceiverAccountId(response.getReceiverAccountId());
        transactionRepository.save(transaction);

        return response;
    }

    private Transaction getTransactionById(UUID transactionId) throws TransactionNotFound {
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFound("Transaction with ID " + transactionId + " not found."));
    }

    private TransactionOutbox getOutboxByTransactionId(String transactionId) throws TransactionNotFound {
        return outboxRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFound("Transaction with ID " + transactionId + " not found."));
    }

    private void ensureNotAlreadySent(TransactionOutbox transaction) throws TransactionAlreadySentException {
        if (transaction.getSent()) {
            throw new TransactionAlreadySentException("Transaction with ID " + transaction.getTransactionId() + " has already been sent.");
        }
    }

    private String sendToWalletServer(String payloadJson) throws FailedToSentException {
        try {
            HttpResponse<String> response = walletServerRequest.instantPaymentRequest(payloadJson);
            return response.body();

        } catch (IOException e) {
            throw new FailedToSentException("Error sending payment to processor occurred while trying to reach Wallet Server. The payment could not be processed!");
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FailedToSentException("An error occurred while we processing your payment request. Please try again later!");
        }
        catch (TimeoutException e) {
            throw new FailedToSentException("Timeout occurred while trying to reach Wallet Server. The request took too long to complete!");
        }
    }
}

