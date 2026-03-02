package com.matheus.payments.wallet.Infra.Kafka.Listeners.UserCreated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Application.Audit.CorrelationId;
import com.matheus.payments.wallet.Application.UseCases.CreateWallet;
import com.matheus.payments.wallet.Infra.Audit.UserCreatedListenerAudit;
import com.matheus.payments.wallet.utils.ApplicationData;
import com.matheus.payments.wallet.utils.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedListener {

    private final CreateWallet createWallet;
    private final ObjectMapper objectMapper;
    private final UserCreatedListenerAudit audit;

    public UserCreatedListener(CreateWallet createWallet, ObjectMapper objectMapper, UserCreatedListenerAudit audit) {
        this.audit = audit;
        this.objectMapper = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.createWallet = createWallet;
    }


    @KafkaListener(topics = KafkaTopics.USER_CREATED_EVENT_TOPIC, groupId = ApplicationData.APPLICATION_CONSUMER_GROUP)
    public void createUserWalletListener(ConsumerRecord<String, String> message, Acknowledgment ack) {
        getCorrelation(message);
        UserCreatedEvent request = null;

        try {
            request = parseMessage(message.value());
            audit.logUserCreatedEventReceived(request.getKeyValue());

            createWallet.createWallet(request);

            audit.logMessageProcessed(request.getKeyValue());
        }
        catch (JsonProcessingException e) {
            audit.logFailedToProcessMessage(message.key(), "Failed to parse UserCreated event: " + e.getMessage());
        }
        finally {
            ack.acknowledge();
            MDC.clear();
        }
    }

    private UserCreatedEvent parseMessage(String message) throws JsonProcessingException {
        return objectMapper.readValue(message, UserCreatedEvent.class);
    }

    private void getCorrelation(ConsumerRecord<String, String> message) {
        String correlationId = new String(message.headers().lastHeader("correlationId").value());
        CorrelationId.set(correlationId); // Set correlationId on MDC to be able to get it on audits logs
    }
}