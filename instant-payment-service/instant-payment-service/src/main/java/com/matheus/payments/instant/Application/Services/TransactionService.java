package com.matheus.payments.instant.Application.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.matheus.payments.instant.Application.DTOs.Request.TransactionRequest;
import com.matheus.payments.instant.Application.Mappers.TransactionMapper;
import com.matheus.payments.instant.Domain.Transaction;
import com.matheus.payments.instant.Infra.Repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMappers;

    public TransactionService
            (TransactionMapper transactionMappers,
             TransactionRepository transactionRepository)
    {
        this.transactionMappers = transactionMappers;
        this.transactionRepository = transactionRepository;

    }

    public String createPaymentProcess(TransactionRequest request) throws JsonProcessingException {

        // Create Transaction Entity
        Transaction transaction = transactionMappers.mapToEntity(request);
        log.info("Create Transaction", LogBuilder.serviceLog("/transaction/pix", "Payment",
                transaction.getTransactionId().toString(), "TransactionService", "createPaymentProcess", "Payment processing starting"));

        transactionRepository.save(transaction);

        return transaction.getTransactionId().toString();

    }
}
