package com.matheus.payments.user_service.Infra.Kafka.Listeners.WalletCreated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.user_service.Application.UseCases.ActivateUserAccount;
import com.matheus.payments.user_service.Utils.ApplicationData;
import com.matheus.payments.user_service.Utils.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class WalletCreatedListener {

    private final ActivateUserAccount activateUserAccount;
    private final ObjectMapper objectMapper;

    public WalletCreatedListener(ActivateUserAccount activateUserAccount, ObjectMapper objectMapper) {
        this.activateUserAccount = activateUserAccount;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.WALLET_CREATED_TOPIC, groupId = ApplicationData.APPLICATION_CONSUMER_NAME)
    public void WalletCreatedEvent(ConsumerRecord<String, String> message, Acknowledgment ack) throws JsonProcessingException {
        WalletCreatedEventDTO payload = objectMapper.readValue(message.value(), WalletCreatedEventDTO.class);
        activateUserAccount.executeActivationUserAccount(payload.getUserId());
        ack.acknowledge();
    }
}
