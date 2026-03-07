package com.matheus.payments.Infra.Kafka.Listeners.DepositFailed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.Application.Audit.CorrelationId;
import com.matheus.payments.Application.Services.DepositService;
import com.matheus.payments.Infra.Audit.DepositFailedListenerAudit;
import com.matheus.payments.Utils.ApplicationData;
import com.matheus.payments.Utils.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DepositFailedListener {

    private final ObjectMapper objectMapper;
    private final DepositService depositService;
    private final DepositFailedListenerAudit audit;


    public DepositFailedListener(DepositService depositService, ObjectMapper objectMapper, DepositFailedListenerAudit audit) {
        this.audit = audit;
        this.objectMapper = objectMapper;
        this.depositService = depositService;
    }

    @KafkaListener(topics = KafkaTopics.DEPOSIT_FAILED_TOPIC, groupId = ApplicationData.APPLICATION_CONSUMER_GROUP)
    public void depositExecutedListener(ConsumerRecord<String, String> message, Acknowledgment ack) {
        getCorrelation(message);
        String depositId = null;
        audit.logMessageConsumedFromKafka();

        try {
            DepositFailed depositExecuted = parseMessage(message.value());
            depositId = depositExecuted.getDepositId().toString();
            audit.logStartDepositUpdate(depositId);
            depositService.setDepositStatusFailed(depositExecuted.getDepositId().toString());
            audit.logDepositUpdatedSuccessfully(depositId);
        }
        catch (Exception e) {
            audit.logDepositUpdatedFailed(depositId, e.getMessage());
        }
        finally {
            ack.acknowledge();
            CorrelationId.clear();
        }
    }

    private DepositFailed parseMessage(String message) throws JsonProcessingException {
        return objectMapper.readValue(message, DepositFailed.class);
    }

    private void getCorrelation(ConsumerRecord<String, String> message) {
        String correlationId = new String(message.headers().lastHeader("correlationId").value());
        CorrelationId.set(correlationId); // Set correlationId on MDC to be able to get it on audits logs
    }
}
