package com.matheus.payments.user_service.Application.Audit;

import com.matheus.payments.user_service.Utils.ApplicationData;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class OutboxServiceAudit {

    private final String CLASS_NAME = "OutboxService";
    private final String METHOD_NAME = "createOutbox";

    public void logStartOutboxCreate(UUID userId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Creating Outbox Entry"));
        logData.add(kv("event", "outbox.creation.starting"));
        logData.add(kv("correlationId", CorrelationId.get()));
        logData.add(kv("userId", userId));

        log.info("Creating Outbox Entry", logData.toArray());
    }

    public void logOutboxCreatedSuccessfully() {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Outbox created successfully"));
        logData.add(kv("event", "outbox.creation.success"));
        logData.add(kv("correlationId", CorrelationId.get()));

        log.info("Outbox created successfully", logData.toArray());
    }

    public void logErrorCreateOutbox(String errorMessage) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Creating Outbox Entry failed"));
        logData.add(kv("event", "outbox.creation.failed"));
        logData.add(kv("correlationId", CorrelationId.get()));
        logData.add(kv("errorMessage", errorMessage));

        log.error("Error creating Outbox Entry", logData.toArray());
    }
}
