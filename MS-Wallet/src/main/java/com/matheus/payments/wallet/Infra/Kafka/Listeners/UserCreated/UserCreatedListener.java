package com.matheus.payments.wallet.Infra.Kafka.Listeners.UserCreated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Application.Audit.CorrelationId;
import com.matheus.payments.wallet.Application.Events.WalletCreatedEvent;
import com.matheus.payments.wallet.Application.Events.WalletCreationFailed;
import com.matheus.payments.wallet.Application.UseCases.CreateWallet;
import com.matheus.payments.wallet.Domain.Exceptions.SocialIdAlreadyExistsException;
import com.matheus.payments.wallet.Infra.Audit.UserCreatedListenerAudit;
import com.matheus.payments.wallet.utils.ApplicationData;
import com.matheus.payments.wallet.utils.KafkaTopics;
import jakarta.persistence.PersistenceException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

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


    @KafkaListener(topics = KafkaTopics.USER_CREATED_EVENT_TOPIC, groupId = ApplicationData.APPLICATION_CONSUMER_GROUP)
    public void createUserWalletListener(ConsumerRecord<String, String> message, Acknowledgment ack) {
        String correlationId = new String(message.headers().lastHeader("correlationId").value());
        CorrelationId.set(correlationId); // Set correlationId on MDC to be able to get it on audits logs
        UserCreatedEvent request = null;

        try {
            request = parseMessage(message.value());
            audit.logUserCreatedEventReceived(request.getKeyValue());

            createWallet.createWallet(request);

            publishSuccess(request);
            audit.logMessageProcessed(request.getKeyValue());

        } catch (JsonProcessingException e) {
            handleFailure(null, "PARSE_ERROR", e);

        } catch (SocialIdAlreadyExistsException e) {
            audit.logFailedToProcessMessage(request.getKeyValue(), e.getMessage());

        } catch (PersistenceException | DataAccessException e) {
            handleFailure(request, "DATABASE_ERROR", e);

        } catch (Exception e) {
            handleFailure(request, "UNKNOWN_ERROR", e);

        } finally {
            ack.acknowledge();
            MDC.clear();
        }
    }

    private UserCreatedEvent parseMessage(String message) throws JsonProcessingException {
        return objectMapper.readValue(message, UserCreatedEvent.class);
    }

    private void publishSuccess(UserCreatedEvent request) {
        internalEventPublisher.publishEvent(
                new WalletCreatedEvent(request.getAccountId(), request.getKeyValue())
        );
    }

    private void handleFailure(UserCreatedEvent request, String errorMessage, Exception e) {
        String keyValue = (request != null) ? request.getKeyValue() : null;
        audit.logFailedToProcessMessage(keyValue, errorMessage + e.getMessage());

        if (request != null) {
            internalEventPublisher.publishEvent(
                    new WalletCreationFailed(request.getAccountId(), request.getKeyValue(), errorMessage)
            );
        }
    }
}
