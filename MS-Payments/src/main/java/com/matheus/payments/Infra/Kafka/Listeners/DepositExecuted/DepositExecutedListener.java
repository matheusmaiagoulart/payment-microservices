package com.matheus.payments.Infra.Kafka.Listeners.DepositExecuted;

import ch.qos.logback.core.util.FixedDelay;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.Application.Audit.CorrelationId;
import com.matheus.payments.Application.Services.DepositService;
import com.matheus.payments.Infra.Audit.DepositExecutedListenerAudit;
import com.matheus.payments.Utils.ApplicationData;
import com.matheus.payments.Utils.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

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
    public void depositExecutedListener(ConsumerRecord<String, String> message, Acknowledgment ack) throws SQLException {
        audit.logMessageConsumedFromKafka();
        String depositId = null;
        try {
            getCorrelation(message);

            DepositExecuted depositExecuted = parseMessage(message.value());
            depositId = depositExecuted.getDepositId().toString();

            audit.logStartDepositUpdate(depositId);

            depositService.setDepositStatusExecuted(depositId);

            audit.logDepositUpdatedSuccessfully(depositId);

            ack.acknowledge();
        }
        catch (JsonProcessingException e) {
            audit.logDepositUpdatedFailed(depositId, e.getMessage());
        }
        finally {
            CorrelationId.clear();
            ack.acknowledge();
        }
    }

    private DepositExecuted parseMessage(String message) throws JsonProcessingException {
        return objectMapper.readValue(message, DepositExecuted.class);
    }

    private void getCorrelation(ConsumerRecord<String, String> message) {
        String correlationId = new String(message.headers().lastHeader("correlationId").value());
        CorrelationId.set(correlationId); // Set correlationId on MDC to be able to get it on audits logs
    }
}
