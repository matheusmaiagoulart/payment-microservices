package com.matheus.payments.wallet.Application.EventHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Application.Audit.CorrelationId;
import com.matheus.payments.wallet.Application.Events.WalletCreatedEvent;
import com.matheus.payments.wallet.Application.Services.OutboxService;
import com.matheus.payments.wallet.utils.ApplicationData;
import com.matheus.payments.wallet.utils.KafkaTopics;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * This class is responsible for handling internal events related to Wallet creation.
 * After a wallet is successfully created, this handler saves the {@link WalletCreatedEvent}
 * to the Outbox for reliable asynchronous publishing to Kafka using the Transactional
 * Outbox Pattern.
 *
 * @author Matheus Maia Goulart
 */
@Slf4j
@Component
public class WalletCreatedInternalEventHandler {

    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;

    public WalletCreatedInternalEventHandler(OutboxService outboxService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.outboxService = outboxService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handler(WalletCreatedEvent event) throws JsonProcessingException {
        try {
            outboxService.createOutbox(event.getUserId(), "WalletCreated", KafkaTopics.WALLET_CREATED_EVENT_TOPIC, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException | DataAccessException | PersistenceException e) {
            log.error("Error while creating outbox entry for WalletCreatedEvent for userId {}: {}", event.getCpf(), e.getMessage(),
                    LogBuilder.eventLog("WalletCreated", KafkaTopics.WALLET_CREATED_EVENT_TOPIC, event.getCpf()),
                    LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), getClass().getName(), "handler", e.getMessage()));
            throw e;
        }

    }
}
