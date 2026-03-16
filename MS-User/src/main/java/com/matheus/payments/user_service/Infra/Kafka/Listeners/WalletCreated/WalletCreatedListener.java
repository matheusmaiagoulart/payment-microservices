package com.matheus.payments.user_service.Infra.Kafka.Listeners.WalletCreated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.user_service.Application.Audit.CorrelationId;
import com.matheus.payments.user_service.Application.Audit.CreateUserAudit;
import com.matheus.payments.user_service.Application.UseCases.ActivateUserAccount;
import com.matheus.payments.user_service.Utils.ApplicationData;
import com.matheus.payments.user_service.Utils.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class WalletCreatedListener {

    private final CreateUserAudit audit;
    private final ObjectMapper objectMapper;
    private final ActivateUserAccount activateUserAccount;

    public WalletCreatedListener(CreateUserAudit audit, ActivateUserAccount activateUserAccount, ObjectMapper objectMapper) {
        this.audit = audit;
        this.objectMapper = objectMapper;
        this.activateUserAccount = activateUserAccount;

    }

    @KafkaListener(topics = KafkaTopics.WALLET_CREATED_TOPIC, groupId = ApplicationData.APPLICATION_CONSUMER_NAME)
    public void WalletCreatedEvent(ConsumerRecord<String, String> message, Acknowledgment ack) throws JsonProcessingException {
        getCorrelation(message);
        WalletCreatedEvent payload = parseMessage(message.value());
        audit.logUserCreatedEventReceived(payload.getCpf());

        activateUserAccount.executeActivationUserAccount(payload.getUserId());
        audit.logUserActivated(payload.getCpf());
        ack.acknowledge();
    }

    private WalletCreatedEvent parseMessage(String message) throws JsonProcessingException {
        return objectMapper.readValue(message, WalletCreatedEvent.class);
    }

    private void getCorrelation(ConsumerRecord<String, String> message) {
        String correlationId = new String(message.headers().lastHeader("correlationId").value());
        CorrelationId.set(correlationId); // Set correlationId on MDC to be able to get it on audits logs
    }
}
