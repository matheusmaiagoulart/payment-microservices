package com.matheus.payments.instant.Application.Audit;

import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class OutboxServiceAudit {

    public static final String REQUEST_FROM = "/transaction/pix";
    public static final String MICROSERVICE_NAME = "Instant-Payment-Service";
    public static final String CLASS_NAME = "OutboxService";

    public void logCreateOutbox(String transactionId) {
        log.info("Creating TransactionOutbox", LogBuilder.serviceLog(REQUEST_FROM, MICROSERVICE_NAME,
                transactionId, CLASS_NAME, "createOutboxEntry", "Payment processing started"));
    }

    public void logErrorCreateOutbox(String transactionId, String errorMessage) {
        log.error("Error to create Outbox Entry", LogBuilder.serviceLog(REQUEST_FROM, MICROSERVICE_NAME,
                        transactionId, CLASS_NAME, "createOutboxEntry", "Payment processing failed"),
                kv("errorMessage", errorMessage));
    }
}
