package com.matheus.payments.wallet.Infra.Kafka.Listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Application.DTOs.Request.CreateWalletRequest;
import com.matheus.payments.wallet.Application.Interfaces.ICreateWallet;
import com.matheus.payments.wallet.Infra.Audit.UserCreatedListenerAudit;
import jakarta.persistence.PersistenceException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class UserCreatedListener {

    private final ICreateWallet createWallet;
    private final ObjectMapper objectMapper;
    private final UserCreatedListenerAudit audit;
    public UserCreatedListener(ICreateWallet createWallet, ObjectMapper objectMapper, UserCreatedListenerAudit audit) {
        this.audit = audit;
        this.objectMapper = objectMapper;
        this.createWallet = createWallet;
    }


    @KafkaListener(topics = "UserCreated", groupId = "wallet-service-group")
    public void createUserWalletListener(ConsumerRecord<String, String> message, Acknowledgment ack) throws JsonProcessingException {

        String keyValue = "";
        try {
            String json = message.value();
            CreateWalletRequest request = objectMapper.readValue(json, CreateWalletRequest.class);
            keyValue = request.getKeyValue();
            audit.logUserCreatedEventReceived(keyValue);
            createWallet.createWallet(request);
            ack.acknowledge();
            audit.logMessageProcessed(keyValue);

        } catch (JsonProcessingException e) {
            audit.logFailedToProcessMessage(keyValue , "Failed to process Kafka message:");
            throw e;
        }
        catch (PersistenceException e) {
            audit.logFailedToProcessMessage(keyValue , "Failed to process Kafka message:");
        }
    }
}
