package com.matheus.payments.user_service.Application.Services;

import com.matheus.payments.user_service.Application.Audit.CorrelationId;
import com.matheus.payments.user_service.Application.Audit.OutboxServiceAudit;
import com.matheus.payments.user_service.Domain.Models.Outbox;
import com.matheus.payments.user_service.Infra.Exceptions.Custom.ErrorToSaveOutboxException;
import com.matheus.payments.user_service.Infra.Repository.OutboxRepository;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class OutboxService {

    private final OutboxServiceAudit audit;
    private final OutboxRepository outboxRepository;

    public OutboxService(OutboxServiceAudit audit, OutboxRepository outboxRepository) {
        this.audit = audit;
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
}