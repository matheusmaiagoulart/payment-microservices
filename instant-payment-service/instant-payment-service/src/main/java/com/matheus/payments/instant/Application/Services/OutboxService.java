package com.matheus.payments.instant.Application.Services;

import com.matheus.payments.instant.Application.Audit.OutboxServiceAudit;
import com.matheus.payments.instant.Domain.TransactionOutbox;
import com.matheus.payments.instant.Infra.Exceptions.Custom.DataBaseException;
import com.matheus.payments.instant.Infra.Repository.OutboxRepository;
import org.springframework.stereotype.Service;

/**
 * Service class is responsible for handling Outbox operations.
 * <p>
 * It's a class that manage details related to Transaction and store related data about the Transaction processing.
 */
@Service
public class OutboxService {

    private final OutboxServiceAudit audit;
    private final OutboxRepository outboxRepository;

    public OutboxService(OutboxRepository outboxRepository, OutboxServiceAudit audit) {
        this.audit = audit;
        this.outboxRepository = outboxRepository;
    }

    public void createOutboxEntry(String transactionId, String payload) {
        audit.logCreateOutbox(transactionId); // LOG

        try {
            // Create Outbox Entry
            TransactionOutbox outbox = new TransactionOutbox(transactionId, payload);
            outboxRepository.save(outbox);

        } catch (DataBaseException e) {
            audit.logErrorCreateOutbox(transactionId, e.getMessage());
            throw new DataBaseException("An error occurred while saving Outbox Entry for transactionId: " + transactionId);
        }

    }
}
