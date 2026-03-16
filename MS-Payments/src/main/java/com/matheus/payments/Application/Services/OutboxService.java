package com.matheus.payments.Application.Services;

import com.matheus.payments.Application.Audit.CorrelationId;
import com.matheus.payments.Application.Audit.OutboxServiceAudit;
import com.matheus.payments.Domain.Exceptions.TransactionNotFound;
import com.matheus.payments.Domain.Models.Deposit;
import com.matheus.payments.Domain.Models.TransactionOutbox;
import com.matheus.payments.Domain.Repositories.OutboxRepository;
import com.matheus.payments.Infra.Exceptions.Custom.DataBaseException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;

/**
 * Service class is responsible for handling Outbox operations.
 * <p>
 * It's a class that manage details related to Transaction and store related data about the Transaction processing.
 */
@Slf4j
@Service
public class OutboxService {

    private final OutboxServiceAudit audit;
    private final OutboxRepository outboxRepository;
    private final DepositService depositService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxService(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate, OutboxServiceAudit audit, DepositService depositService) {
        this.audit = audit;
        this.kafkaTemplate = kafkaTemplate;
        this.outboxRepository = outboxRepository;
        this.depositService = depositService;
    }

    @Retry(name = "databaseRetry")
    @Transactional(propagation = Propagation.REQUIRED)
    public void createOutboxEntry(String transactionId, String payload, String topic) {
        audit.logCreateOutbox(transactionId); // LOG

        try {
            TransactionOutbox outbox = new TransactionOutbox(transactionId, payload, CorrelationId.get(), topic);
            save(outbox);

        } catch (DataBaseException e) {
            audit.logErrorCreateOutbox(transactionId, e.getMessage());
            throw new DataBaseException("An error occurred while saving Outbox Entry for transactionId: " + transactionId);
        }
    }

    public TransactionOutbox getOutboxByTransactionId(String transactionId) throws TransactionNotFound {
        return outboxRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFound(transactionId));
    }

    @Transactional
    public void save(TransactionOutbox outbox) {
        try {
            outboxRepository.save(outbox);
        } catch (DataBaseException e) {
            audit.logErrorCreateOutbox(outbox.getTransactionId(), e.getMessage());
            throw new DataBaseException("An error occurred while saving Outbox Entry for transactionId: " + outbox.getTransactionId());
        }
    }

    @CircuitBreaker(name = "defaultCircuitBreaker", fallbackMethod = "handleErrorToSendOutboxEvent")
    @Retry(name = "kafkaRetry")
    @Transactional
    public void sendOutboxEntry(TransactionOutbox outbox) throws ExecutionException, InterruptedException {
        Message<String> message = MessageBuilder
                .withPayload(outbox.getPayload())
                .setHeader("correlationId", outbox.getCorrelationId().toString())
                .setHeader(KafkaHeaders.TOPIC, outbox.getTopic())
                .build();
        kafkaTemplate.send(message).get();
        setOutboxSent(outbox);
        depositService.updateDepositStatus(outbox.getTransactionId(), Deposit.DepositStatus.SENT);
    }

    private void setOutboxSent(TransactionOutbox outbox) {
        outbox.setSent(true);
        save(outbox);
    }

    private void setOutboxFailed(TransactionOutbox outbox, String errorMessage) {
        outbox.setFailed(true);
        outbox.setFailureReason(errorMessage);
        save(outbox);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected void handleErrorToSendOutboxEvent(TransactionOutbox outbox, Throwable throwable) {
        log.warn("Fallback: Circuit Breaker opened. Cause: {}", throwable.getMessage());
        setOutboxFailed(outbox, "Circuit breaker opened, the message was not sent. Cause: " + throwable.getCause());
    }


}
