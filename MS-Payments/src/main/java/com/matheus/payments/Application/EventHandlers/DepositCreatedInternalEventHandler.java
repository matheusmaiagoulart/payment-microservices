package com.matheus.payments.Application.EventHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.Application.Audit.CorrelationId;
import com.matheus.payments.Domain.Events.DepositCreatedEvent;
import com.matheus.payments.Application.Services.OutboxService;
import com.matheus.payments.Utils.ApplicationData;
import com.matheus.payments.Utils.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class DepositCreatedInternalEventHandler {

    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    public DepositCreatedInternalEventHandler(OutboxService outboxService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.outboxService = outboxService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handler(DepositCreatedEvent event) throws JsonProcessingException {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            outboxService.createOutboxEntry(event.getDepositId().toString(), eventJson, KafkaTopics.DEPOSIT_TOPIC);
        }
        catch (JsonProcessingException | DataAccessException e) {
            log.error("Error while creating outbox entry for WalletCreatedEvent for userId {}: {}", event.getDepositId(), e.getMessage(),
                    LogBuilder.eventLog("WalletCreated", KafkaTopics.DEPOSIT_TOPIC, event.getDepositId().toString()),
                    LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), getClass().getName(), "handler", e.getMessage()));
            throw e;
        }
    }
}
