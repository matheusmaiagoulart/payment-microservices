package com.matheus.payments.wallet.Application.EventHandlers.WalletHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Domain.Events.CreateWallet.WalletCreationFailed;
import com.matheus.payments.wallet.Application.Services.OutboxService;
import com.matheus.payments.wallet.utils.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class WalletCreationFailedEventHandler {

    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;

    public WalletCreationFailedEventHandler(OutboxService outboxService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.outboxService = outboxService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handler(WalletCreationFailed event) throws JsonProcessingException {
        try {
            outboxService.createOutbox(event.getUserId(), "WalletCreationFailed", KafkaTopics.WALLET_CREATION_FAILED_TOPIC, objectMapper.writeValueAsString(event));
            log.info("WalletCreation fail reason: {}", event.getErrorMessage());
        } catch (JsonProcessingException e) {
            log.error("Failed to create outbox entry for WalletCreationFailed event for userId {}: {}", event.getUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating outbox entry for WalletCreationFailed event for userId {}: {}", event.getUserId(), e.getMessage());
            throw e;
        }
    }
}
