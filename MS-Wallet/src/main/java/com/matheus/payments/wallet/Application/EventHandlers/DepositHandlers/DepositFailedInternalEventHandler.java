package com.matheus.payments.wallet.Application.EventHandlers.DepositHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Application.Audit.CorrelationId;
import com.matheus.payments.wallet.Application.Events.Deposit.DepositExecuted;
import com.matheus.payments.wallet.Application.Events.Deposit.DepositFailed;
import com.matheus.payments.wallet.Application.Services.OutboxService;
import com.matheus.payments.wallet.utils.ApplicationData;
import com.matheus.payments.wallet.utils.KafkaTopics;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class DepositFailedInternalEventHandler {

    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;

    public DepositFailedInternalEventHandler(OutboxService outboxService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.outboxService = outboxService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handle(DepositFailed event) throws JsonProcessingException {
        try {
            outboxService.createOutbox(event.getReceiverId(), "DepositFailed", KafkaTopics.DEPOSIT_EXECUTED_TOPIC, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException | DataAccessException | PersistenceException e) {
            log.error("Error while creating outbox entry for DepositFailed for userId {}: {}", event.getReceiverId(), e.getMessage(),
                    LogBuilder.eventLog("DepositExecuted", KafkaTopics.DEPOSIT_EXECUTED_TOPIC, event.getReceiverId().toString()),
                    LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), getClass().getName(), "handler", e.getMessage()));
            throw e;
        }
    }
}

