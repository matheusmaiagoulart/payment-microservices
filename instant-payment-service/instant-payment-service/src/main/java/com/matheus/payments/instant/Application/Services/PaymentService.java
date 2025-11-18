package com.matheus.payments.instant.Application.Services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.instant.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.instant.Application.DTOs.TransactionRequest;
import com.matheus.payments.instant.Application.Mappers.Transaction.TransactionMapper;
import com.matheus.payments.instant.Domain.Transaction.Transaction;
import com.matheus.payments.instant.Domain.Transaction.TransactionOutbox;
import com.matheus.payments.instant.Domain.Transaction.TransactionStatus;
import com.matheus.payments.instant.Infra.Exceptions.Custom.*;
import com.matheus.payments.instant.Infra.Http.Clients.WalletServer;
import com.matheus.payments.instant.Infra.Repository.OutboxRepository;
import com.matheus.payments.instant.Infra.Repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class PaymentService {


    private TransactionMapper transactionMappers;
    private TransactionRepository transactionRepository;
    private OutboxRepository outboxRepository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private WalletServer walletServerRequest;
    private ObjectMapper objectMapper;

    public PaymentService
            (
                    TransactionMapper transactionMappers,
                    TransactionRepository transactionRepository,
                    OutboxRepository outboxRepository,
                    KafkaTemplate<String, String> kafkaTemplate,
                    ObjectMapper objectMapper,
                    WalletServer walletServerRequest
            ) {
        this.transactionMappers = transactionMappers;
        this.transactionRepository = transactionRepository;
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.walletServerRequest = walletServerRequest;
    }



    public PaymentProcessorResponse paymentOrchestration(TransactionRequest request) throws IOException, InterruptedException {

        String transactionId = createPaymentProcess(request);

        System.out.println("Enviando para o processador de pagamentos: " + transactionId);

        String processorResponse = sendPaymentToProcessor(transactionId);

        return paymentStatusUpdate(processorResponse);
    }

    private String createPaymentProcess(TransactionRequest request) throws JsonProcessingException {

        System.out.println("Enviada solicitação de pagamento: " + request);

        // Create Transaction Entity
        Transaction transaction = transactionMappers.mapToEntity(request);
        transactionRepository.save(transaction);

        // Create Outbox Entry
        TransactionOutbox outbox = new TransactionOutbox(transaction.getTransactionId().toString(), objectMapper.writeValueAsString(transaction));
        outboxRepository.save(outbox);

        return transaction.getTransactionId().toString();

    }


    private String sendPaymentToProcessor(String transactionId) throws IOException {

        var transactionOutbox = outboxRepository.findByTransactionId(transactionId);
        if (transactionOutbox.isEmpty()) {
            throw new TransactionNotFound("Transaction with ID " + transactionId + " not found.");
        }

        TransactionOutbox outbox = transactionOutbox.get();

        if (outbox.getSent()) {
            throw new TransactionAlreadySentException("Transaction with ID " + transactionId + " has already been sent.");
        }

        String jsonPayload = outbox.getPayload();
        HttpResponse<String> response;
        try{
            response = walletServerRequest.instantPaymentRequest(jsonPayload);
        } catch (IOException | InterruptedException e) {

            System.out.println("Error sending payment to processAn error occurred while trying to reach Wallet Server. The payment could not be processed: " + e.getMessage());
             PaymentProcessorResponse failedResponse = new PaymentProcessorResponse(
                    UUID.fromString(transactionId),
                    false,
                    false,
                    true,
                     ("An error occurred." + e.getMessage())
            );
            String failedResponseJson = objectMapper.writeValueAsString(failedResponse);
            paymentStatusUpdate(failedResponseJson);

            throw new FailedToSentException("Error sending payment to processAn error occurred while trying to reach Wallet Server. The payment could not be processed.");

        }

        outbox.setSent(true);
        outboxRepository.save(outbox);
        return response.body();

    }


    private PaymentProcessorResponse paymentStatusUpdate(String message) throws JsonProcessingException {


        PaymentProcessorResponse response = objectMapper.readValue(message, PaymentProcessorResponse.class);

        System.out.println("Payment status update received: " + response);

        Transaction transaction = transactionRepository.findByTransactionId(response.getTransactionId()).orElseThrow(()
                -> new TransactionNotFound("Transaction with ID " + response.getTransactionId() + " not found."));


        if (!response.getIsSucessful()) {
            TransactionOutbox outbox = outboxRepository.findByTransactionId(response.getTransactionId().toString()).orElseThrow(() ->
                    new TransactionNotFound("Outbox entry for Transaction ID " + response.getTransactionId() + " not found."));

            outbox.setFailed(true);
            outbox.setFailureReason(response.getFailedMessage());
            outbox.setFailureAt(LocalDateTime.now());
            outboxRepository.save(outbox);

            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

           throw new TransactionFailedException(response.getFailedMessage());
        }
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

        return response;
    }


}



