package com.matheus.payments.wallet.Infra.Kafka.Listeners.DepositCreated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Application.Audit.CorrelationId;
import com.matheus.payments.wallet.Application.Audit.DepositAudit;
import com.matheus.payments.wallet.Application.UseCases.Deposit;
import com.matheus.payments.wallet.utils.ApplicationData;
import com.matheus.payments.wallet.utils.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class DepositCreatedListener {

    private final DepositAudit audit;
    private final Deposit depositUseCase;
    private final ObjectMapper objectMapper;

    public DepositCreatedListener(ObjectMapper objectMapper, Deposit depositUseCase, DepositAudit audit) {
        this.audit = audit;
        this.objectMapper = objectMapper;
        this.depositUseCase = depositUseCase;

    }

    @KafkaListener(topics = KafkaTopics.DEPOSIT_TOPIC, groupId = ApplicationData.APPLICATION_CONSUMER_GROUP)
    public void depositCreatedListener(ConsumerRecord<String, String> message, Acknowledgment ack) throws JsonProcessingException {
        getCorrelation(message);
        DepositCreated event = null;
        audit.logEventReceived();

        try {
            event = parseMessage(message.value());
            depositUseCase.executeDeposit(event);
        } catch (JsonProcessingException e) {
            throw e;
        } finally {
            ack.acknowledge();
            MDC.clear();
        }
    }

    private DepositCreated parseMessage(String message) throws JsonProcessingException {
        return objectMapper.readValue(message, DepositCreated.class);
    }

    private void getCorrelation(ConsumerRecord<String, String> message) {
        String correlationId = new String(message.headers().lastHeader("correlationId").value());
        CorrelationId.set(correlationId); // Set correlationId on MDC to be able to get it on audits logs
    }
}
