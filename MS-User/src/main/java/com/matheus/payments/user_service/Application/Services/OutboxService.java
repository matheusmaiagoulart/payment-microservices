package com.matheus.payments.user_service.Application.Services;

import com.matheus.payments.user_service.Application.Audit.CorrelationId;
import com.matheus.payments.user_service.Application.Audit.OutboxServiceAudit;
import com.matheus.payments.user_service.Domain.Models.Outbox;
import com.matheus.payments.user_service.Domain.Repositories.OutboxRepository;
import com.matheus.payments.user_service.Infra.Exceptions.Custom.ErrorToSaveOutboxException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class OutboxService {

    private final OutboxServiceAudit audit;
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> publisher;

    public OutboxService(OutboxServiceAudit audit, OutboxRepository outboxRepository, KafkaTemplate<String, String> publisher) {
        this.audit = audit;
        this.publisher = publisher;
        this.outboxRepository = outboxRepository;
    }

    @Retry(name = "databaseRetry", fallbackMethod = "handleErrorToSaveOutboxEvent")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createOutbox(UUID userId, String eventType, String topic, String payload) {
        audit.logStartOutboxCreate(userId);
        outboxRepository.save(new Outbox(userId, eventType, topic, payload, CorrelationId.get()));
        audit.logOutboxCreatedSuccessfully();
    }

    protected void handleErrorToSaveOutboxEvent(UUID userId, String eventType, String topic, String payload, Throwable throwable) {
        log.warn("Fallback: Error to save Outbox for userId: {}, eventType: {}. Cause: {}",
                userId, eventType, throwable.getMessage());
        throw new ErrorToSaveOutboxException();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // Always create a new transaction for each outbox event
    public void sendOutboxEvent(Outbox outbox) {
        try {
            Message<String> message = MessageBuilder
                    .withPayload(outbox.getPayload())
                    .setHeader("correlationId", outbox.getCorrelationId().toString())
                    .setHeader(KafkaHeaders.TOPIC, outbox.getTopic())
                    .build();

            publisher.send(message);
            setOutboxSent(outbox);
        } catch (Exception e) {
            setOutboxFailed(outbox, e.getMessage());
            outboxRepository.save(outbox);
        }
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
}