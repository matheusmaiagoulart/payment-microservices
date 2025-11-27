package com.matheus.payments.instant.Application.Services.InstantPaymentServices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.instant.Application.DTOs.Request.TransactionRequest;
import com.matheus.payments.instant.Application.Mappers.TransactionMapper;
import com.matheus.payments.instant.Domain.Transaction;
import com.matheus.payments.instant.Domain.TransactionOutbox;
import com.matheus.payments.instant.Infra.Repository.OutboxRepository;
import com.matheus.payments.instant.Infra.Repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMappers;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public TransactionService(
            TransactionRepository transactionRepository,
            TransactionMapper transactionMappers,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.transactionMappers = transactionMappers;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }


    public String createPaymentProcess(TransactionRequest request) throws JsonProcessingException {

        System.out.println("Enviada solicitação de pagamento: " + request);

        // Create Transaction Entity
        Transaction transaction = transactionMappers.mapToEntity(request);
        transactionRepository.save(transaction);

        // Create Outbox Entry
        TransactionOutbox outbox = new TransactionOutbox(transaction.getTransactionId().toString(), objectMapper.writeValueAsString(transaction));
        outboxRepository.save(outbox);

        return transaction.getTransactionId().toString();

    }
}
