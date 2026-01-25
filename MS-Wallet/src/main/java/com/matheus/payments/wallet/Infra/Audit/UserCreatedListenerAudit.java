package com.matheus.payments.wallet.Infra.Audit;

import com.matheus.payments.wallet.Application.Audit.CorrelationId;
import com.matheus.payments.wallet.utils.ApplicationData;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class UserCreatedListenerAudit {

    private final String CLASS_NAME = "UserCreatedListener";
    private final String METHOD_NAME = "createUserWalletListener";
    private final String TOPIC_NAME = "UserCreated";
    private final String EVENT_NAME = "UserCreated";

    public void logUserCreatedEventReceived(String keyValue) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Received UserCreated event for key:" + keyValue));
        logData.addAll(LogBuilder.eventLog(EVENT_NAME, TOPIC_NAME, keyValue));
        logData.add(kv("event", "user_created.event.received"));

        log.info("Received UserCreated event for key: " + keyValue, logData.toArray());
    }

    public void logMessageProcessed(String keyValue) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME,
                "Successfully processed UserCreated event for key:" + keyValue));
        logData.addAll(LogBuilder.eventLog(EVENT_NAME, TOPIC_NAME, keyValue));
        logData.add(kv("event", "user_created.event.processed"));

        log.info("Successfully processed UserCreated event for key: " + keyValue, logData.toArray());
    }

    public void logFailedToProcessMessage(String keyValue, String errorMessage) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME,
                "Failed to process UserCreated event for key:" + keyValue));
        logData.addAll(LogBuilder.eventLog(EVENT_NAME, TOPIC_NAME, keyValue));
        logData.add(kv("event", "user_created.event.processing.failed"));
        logData.add(kv("errorMessage", errorMessage));

        log.error("Failed to process UserCreated event for key: " + keyValue, logData.toArray());
    }

}
