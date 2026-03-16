package com.matheus.payments.user_service.Application.EventHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.user_service.Application.Audit.CorrelationId;
import com.matheus.payments.user_service.Application.Services.OutboxService;
import com.matheus.payments.user_service.Domain.Events.UserCreatedEvent;
import com.matheus.payments.user_service.Utils.ApplicationData;
import com.matheus.payments.user_service.Utils.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * This class is responsible for handling internal events related to User creation.
 * After User is successfully created, this handler saves the {@link UserCreatedEvent}
 * to the Outbox for reliable asynchronous publishing to Kafka using the Transactional
 *
 * @author Matheus Maia Goulart
 */
@Slf4j
@Component
public class UserCreatedInternalEventHandler {

    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    public UserCreatedInternalEventHandler(OutboxService outboxService, ObjectMapper objectMapper) {
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handler(UserCreatedEvent event) throws JsonProcessingException {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxService.createOutbox(event.getUserId(), "UserCreated", KafkaTopics.USER_CREATED, payload);
        } catch (Exception e) {
            log.error("Failed to process UserCreatedEvent for userId {} after retries", event.getUserId(),
                    LogBuilder.eventLog("UserCreated", KafkaTopics.USER_CREATED, event.getCpf()),
                    LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), getClass().getName(), "handler", e.getMessage()));
        }

    }
}
