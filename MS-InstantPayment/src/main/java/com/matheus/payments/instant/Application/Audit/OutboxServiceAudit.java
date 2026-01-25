package com.matheus.payments.instant.Application.Audit;

import com.matheus.payments.instant.Utils.ApplicationData;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class OutboxServiceAudit {

    public static final String REQUEST_FROM = "/transaction/pix";
    public static final String CLASS_NAME = "OutboxService";


    public void logCreateOutbox(String transactionId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "createOutboxEntry", "Payment processing started"));
        logData.addAll(LogBuilder.requestLog("POST", REQUEST_FROM));
        logData.add(kv("event", "outbox.creation.starting"));
        logData.add(kv("transactionId", transactionId));

        log.info("Creating TransactionOutbox", logData.toArray());
    }

    public void logErrorCreateOutbox(String transactionId, String errorMessage) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "createOutboxEntry", "Payment processing failed"));
        logData.addAll(LogBuilder.requestLog("POST", REQUEST_FROM));
        logData.add(kv("event", "outbox.creation.failed"));
        logData.add(kv("transactionId", transactionId));
        logData.add(kv("errorMessage", errorMessage));

        log.error("Error creating Outbox Entry", logData.toArray());
    }
}
