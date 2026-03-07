package com.matheus.payments.Infra.Audit;

import com.matheus.payments.Application.Audit.CorrelationId;
import com.matheus.payments.Utils.ApplicationData;
import com.matheus.payments.Utils.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class DepositFailedListenerAudit {

    public static final String CLASS_NAME = "DepositFailedListener";
    public static final String EVENT_NAME = "DepositFailed";

    public void logMessageConsumedFromKafka() {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "depositExecutedListener", "Message consumed from Kafka topic"));
        logData.add(kv("event", "payment.process.deposit.updating.status"));
        logData.add(kv("correlationId", CorrelationId.get()));

        log.info("Message consumed from Kafka Topic", logData.toArray());
    }

    public void logStartDepositUpdate(String depositId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "depositExecutedListener", "Start status updating for deposit."));
        logData.add(LogBuilder.eventLog(EVENT_NAME, KafkaTopics.DEPOSIT_EXECUTED_TOPIC, depositId));
        logData.add(kv("event", "payment.process.deposit.updating.status"));
        logData.add(kv("correlationId", CorrelationId.get()));
        logData.add(kv("depositId", depositId));

        log.info("Start status updating for deposit. ", logData.toArray());
    }

    public void logDepositUpdatedSuccessfully(String depositId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "depositExecutedListener", "Status updated successfully."));
        logData.add(LogBuilder.eventLog(EVENT_NAME, KafkaTopics.DEPOSIT_EXECUTED_TOPIC, depositId));
        logData.add(kv("event", "payment.process.updating.deposit.status.successfully"));
        logData.add(kv("correlationId", CorrelationId.get()));
        logData.add(kv("depositId", depositId));

        log.info("Status updated successfully.", logData.toArray());
    }

    public void logDepositUpdatedFailed(String depositId, String failedReason) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "depositExecutedListener", String.format("Status updating failed for reason: %s", failedReason)));
        logData.add(LogBuilder.eventLog(EVENT_NAME, KafkaTopics.DEPOSIT_EXECUTED_TOPIC, depositId));
        logData.add(kv("event", "payment.process.updating.deposit.status.failed"));
        logData.add(kv("correlationId", CorrelationId.get()));
        logData.add(kv("depositId", depositId));
        logData.add(kv("failureReason", failedReason));

        log.info("Status updating failed.", logData.toArray());
    }
}
