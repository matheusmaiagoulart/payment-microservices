package com.matheus.payments.wallet.Application.EventHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Application.Events.WalletCreationFailed;
import com.matheus.payments.wallet.Application.Services.OutboxService;
import com.matheus.payments.wallet.utils.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WalletCreationFailedEventHandler {

    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;

    public WalletCreationFailedEventHandler(OutboxService outboxService, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.outboxService = outboxService;
    }

    @EventListener
    public void handler(WalletCreationFailed event) throws JsonProcessingException {
        outboxService.createOutbox(event.getUserId(), "WalletCreationFailed", KafkaTopics.WALLET_CREATION_FAILED_TOPIC, objectMapper.writeValueAsString(event));
    }
}
