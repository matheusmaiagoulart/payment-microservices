package com.matheus.payments.Application.Services;

import com.matheus.payments.Application.Audit.TransactionServiceAudit;
import com.matheus.payments.Application.DTOs.TransactionRequest;
import com.matheus.payments.Application.Mappers.TransactionMapper;
import com.matheus.payments.Domain.Exceptions.TransactionNotFound;
import com.matheus.payments.Domain.Models.Transaction;
import com.matheus.payments.Domain.Repositories.TransactionRepository;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Retry(name = "databaseRetry", fallbackMethod = "fallbackCreatePaymentProcess")
    @Transactional
    public String createPaymentProcess(TransactionRequest request) {
            // Create Transaction Entity
            Transaction transaction = transactionMappers.mapToEntity(request);
            UUID transactionId = transaction.getTransactionId();

            audit.logCreateTransaction(transactionId.toString()); // LOG

            transactionRepository.save(transaction);
            return transaction.getTransactionId().toString();
    }

    public Transaction getTransactionById(UUID transactionId) throws TransactionNotFound {
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFound(transactionId.toString()));
    }

    @Retry(name = "databaseRetry", fallbackMethod = "fallbackSave")
    @Transactional
    public void save(Transaction transaction) {
            transactionRepository.save(transaction);
    }

    private String fallbackCreatePaymentProcess(TransactionRequest request, Throwable t) throws Throwable {
        audit.logErrorCreateTransaction(t.getMessage());
        throw t;
    }

    private void fallbackSave(Transaction transaction, Throwable t) throws Throwable {
        audit.logErrorCreateTransaction(t.getMessage());
        throw t;
    }


}
