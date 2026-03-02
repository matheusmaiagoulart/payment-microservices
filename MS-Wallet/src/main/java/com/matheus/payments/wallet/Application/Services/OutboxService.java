package com.matheus.payments.wallet.Application.Services;

import com.matheus.payments.wallet.Application.Audit.CorrelationId;
import com.matheus.payments.wallet.Domain.Models.Outbox;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.ErrorToSaveOutboxException;
import com.matheus.payments.wallet.Infra.Repository.OutboxRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> publisher;

    public OutboxService(OutboxRepository outboxRepository, KafkaTemplate<String, String> publisher) {
        this.publisher = publisher;
        this.outboxRepository = outboxRepository;

    }
    @Retry(name = "databaseRetry", fallbackMethod = "handleErrorToSaveOutboxEvent")
    @Transactional(propagation = Propagation.REQUIRED)
    public void createOutbox(UUID userId, String eventType, String topic, String payload) {
        Outbox outbox = new Outbox(userId, CorrelationId.get(), eventType, topic, payload);
        outboxRepository.save(outbox);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @CircuitBreaker(name = "outboxScheduler", fallbackMethod = "handleErrorToSendOutboxEvent")
    @Retry(name = "databaseRetry", fallbackMethod = "handleErrorToSendOutboxEventRetry")
    public void sendOutboxEvent(Outbox outbox) throws ExecutionException, InterruptedException {
        publisher.send(outbox.getTopic(), outbox.getPayload());
        setOutboxSent(outbox);
    }

    private void setOutboxSent(Outbox outbox) {
        outbox.setSent(true);
        outboxRepository.save(outbox);
    }

    private void setOutboxFailed(Outbox outbox, String errorMessage) {
        outbox.setFailed(true);
        outbox.setFailureReason(errorMessage);
        outboxRepository.save(outbox);
    }

    // Fallback method for Circuit Breaker

    @Transactional(propagation = Propagation.REQUIRED)
    private void handleErrorToSendOutboxEvent(Outbox outbox, Throwable throwable) {
        setOutboxFailed(outbox, "Circuit breaker opened, the message was not sent. Cause: " + throwable.getCause());
    }

    private void handleErrorToSaveOutboxEvent(UUID userId, String eventType, String topic, String payload, Throwable throwable) {
        log.error("Fallback: Error to save Outbox for userId: {}, eventType: {}. Cause: {}",
                userId, eventType, throwable.getMessage());
        throw new ErrorToSaveOutboxException();
    }

    private void handleErrorToSendOutboxEventRetry(Outbox outbox, Throwable throwable) {
        log.error("Retry fallback: Error to send Outbox id: {}. Cause: {}",
                outbox.getId(), throwable.getMessage());
        setOutboxFailed(outbox, "Retry failed: " + throwable.getMessage());
    }
}
