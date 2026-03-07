package com.matheus.payments.Infra.Kafka.Listeners.DepositExecuted;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.Application.Audit.CorrelationId;
import com.matheus.payments.Application.Services.DepositService;
import com.matheus.payments.Infra.Audit.DepositExecutedListenerAudit;
import com.matheus.payments.Utils.ApplicationData;
import com.matheus.payments.Utils.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DepositExecutedListener {
    private final ObjectMapper objectMapper;
    private final DepositService depositService;
    private final DepositExecutedListenerAudit audit;


    public DepositExecutedListener(DepositService depositService, ObjectMapper objectMapper, DepositExecutedListenerAudit audit) {
        this.audit = audit;
        this.objectMapper = objectMapper;
        this.depositService = depositService;
    }

    @KafkaListener(topics = KafkaTopics.DEPOSIT_EXECUTED_TOPIC, groupId = ApplicationData.APPLICATION_CONSUMER_GROUP)
    public void depositExecutedListener(ConsumerRecord<String, String> message, Acknowledgment ack) {
        getCorrelation(message);
        audit.logMessageConsumedFromKafka();
        String depositId = null;

        try {
            DepositExecuted depositExecuted = parseMessage(message.value());
            depositId = depositExecuted.getDepositId().toString();

            audit.logStartDepositUpdate(depositId);

            depositService.setDepositStatusExecuted(depositId);

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

    private DepositExecuted parseMessage(String message) throws Exception {
        return objectMapper.readValue(message, DepositExecuted.class);
    }

    private void getCorrelation(ConsumerRecord<String, String> message) {
        String correlationId = new String(message.headers().lastHeader("correlationId").value());
        CorrelationId.set(correlationId); // Set correlationId on MDC to be able to get it on audits logs
    }
}
