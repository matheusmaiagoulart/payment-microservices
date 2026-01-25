package com.matheus.payments.wallet.Infra.Kafka.Listeners.UserCreated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Application.Audit.CorrelationId;
import com.matheus.payments.wallet.Application.Events.WalletCreatedEvent;
import com.matheus.payments.wallet.Application.UseCases.CreateWallet;
import com.matheus.payments.wallet.Infra.Audit.UserCreatedListenerAudit;
import com.matheus.payments.wallet.utils.ApplicationData;
import com.matheus.payments.wallet.utils.KafkaTopics;
import jakarta.persistence.PersistenceException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserCreatedListener {

    private final CreateWallet createWallet;
    private final ObjectMapper objectMapper;
    private final UserCreatedListenerAudit audit;
    private final ApplicationEventPublisher internalEventPublisher;

    public UserCreatedListener(CreateWallet createWallet, ObjectMapper objectMapper, UserCreatedListenerAudit audit, ApplicationEventPublisher internalEventPublisher) {
        this.audit = audit;
        this.objectMapper = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.createWallet = createWallet;
        this.internalEventPublisher = internalEventPublisher;
    }


    @Transactional
    @KafkaListener(topics = KafkaTopics.USER_CREATED_EVENT_TOPIC, groupId = ApplicationData.APPLICATION_CONSUMER_GROUP)
    public void createUserWalletListener(ConsumerRecord<String, String> message, Acknowledgment ack) throws JsonProcessingException, PersistenceException {
        String keyValue = "";
        String correlationId = new String(message.headers().lastHeader("correlationId").value());
        CorrelationId.set(correlationId); // Set correlationId on MDC to be able to get it on audits logs
        try {
            UserCreatedEvent request = objectMapper.readValue(message.value(), UserCreatedEvent.class);
            keyValue = request.getKeyValue();
            audit.logUserCreatedEventReceived(keyValue);

            createWallet.createWallet(request);

            internalEventPublisher.publishEvent(new WalletCreatedEvent(request.getAccountId(), request.getKeyValue()));
            audit.logMessageProcessed(keyValue);
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            audit.logFailedToProcessMessage(keyValue, e.getMessage());

        } catch (PersistenceException e) {
            audit.logFailedToProcessMessage(keyValue, e.getMessage());
            throw e; // Propaga para retry do Kafka
        } finally {
            MDC.clear();
        }
    }
}
