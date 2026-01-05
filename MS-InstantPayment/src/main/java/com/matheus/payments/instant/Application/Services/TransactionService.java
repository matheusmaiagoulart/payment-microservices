package com.matheus.payments.instant.Application.Services;

import com.matheus.payments.instant.Application.Audit.TransactionServiceAudit;
import com.matheus.payments.instant.Application.DTOs.Request.TransactionRequest;
import com.matheus.payments.instant.Application.Mappers.TransactionMapper;
import com.matheus.payments.instant.Domain.Transaction;
import com.matheus.payments.instant.Infra.Exceptions.Custom.DataBaseException;
import com.matheus.payments.instant.Infra.Repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service class is responsible for handling Transaction operations.
 * <p>
 * It's starting the process of creating a new Transaction
 *
 * @author Matheus Maia Goulart
 */

@Service
public class TransactionService {

    private final TransactionServiceAudit audit;
    private final TransactionMapper transactionMappers;
    private final TransactionRepository transactionRepository;

    public TransactionService
            (TransactionServiceAudit audit,
             TransactionMapper transactionMappers,
             TransactionRepository transactionRepository)
    {
        this.audit = audit;
        this.transactionMappers = transactionMappers;
        this.transactionRepository = transactionRepository;

    }

    public String createPaymentProcess(TransactionRequest request) {
        try {
            // Create Transaction Entity
            Transaction transaction = transactionMappers.mapToEntity(request);
            UUID transactionId = transaction.getTransactionId();

            audit.logCreateTransaction(transactionId.toString()); // LOG

            transactionRepository.save(transaction);
            return transaction.getTransactionId().toString();

        } catch (DataBaseException e) {
            audit.logErrorCreateTransaction(e.getMessage());
            throw new DataBaseException("An error occurred while saving Transaction for request: " + request.toString());
        }
    }
}
