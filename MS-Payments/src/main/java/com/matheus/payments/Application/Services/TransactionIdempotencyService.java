package com.matheus.payments.Application.Services;

import com.matheus.payments.Application.Audit.CorrelationId;
import com.matheus.payments.Application.Audit.TransactionIdempotencyServiceAudit;
import com.matheus.payments.Domain.Exceptions.TransactionNotFound;
import com.matheus.payments.Domain.Models.TransactionIdempotency;
import com.matheus.payments.Domain.Repositories.TransactionIdempotencyRepository;
import com.matheus.payments.Infra.Exceptions.Custom.DataBaseException;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionIdempotencyService {

    private final TransactionIdempotencyServiceAudit audit;
    private final TransactionIdempotencyRepository transactionRepository;

    public TransactionIdempotencyService(TransactionIdempotencyServiceAudit audit, TransactionIdempotencyRepository transactionRepository) {
        this.audit = audit;
        this.transactionRepository = transactionRepository;
    }

    @Retry(name = "databaseRetry", fallbackMethod = "fallbackCreateTransactionIdempotencyEntry")
    @Transactional
    public void createTransactionIdempotencyEntry(String transactionId, String payload) {
        audit.logCreateTransactionalIdempotencyEntry(transactionId); // LOG
        TransactionIdempotency outbox = new TransactionIdempotency(UUID.fromString(transactionId), payload, CorrelationId.get());
        save(outbox);
    }

    @Transactional
    public void setTransactionSent(TransactionIdempotency transaction) {
        transaction.setSent(true);
        save(transaction);
    }

    public TransactionIdempotency getByTransactionId(UUID transactionId) {
        return transactionRepository.getByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFound(transactionId.toString()));
    }

    @Transactional
    public void save(TransactionIdempotency transaction) {
        try {
            transactionRepository.save(transaction);
        } catch (DataBaseException e) {
            throw new DataBaseException("An error occurred while saving Transaction Idempotency with Transaction Id: " + transaction.getTransactionId());
        }
    }

    @Transactional
    public void setTransactionIdempotencyFailed(TransactionIdempotency transaction, String errorMessage) {
        transaction.setFailed(true);
        transaction.setFailureReason(errorMessage);
        save(transaction);
    }

    private void fallbackCreateTransactionIdempotencyEntry(String transactionId, String payload, Throwable e) {
        audit.logErrorToCreateTransactionIdempotencyEntry(transactionId, e.getMessage());
        throw new DataBaseException("An error occurred while saving TransactionIdempotency Entry for transactionId: " + transactionId);
    }
}
