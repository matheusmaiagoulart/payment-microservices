package com.matheus.payments.Application.Audit;

import com.matheus.payments.Utils.ApplicationData;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class TransactionIdempotencyServiceAudit {

    public static final String REQUEST_FROM = "/transaction/pix";
    public static final String CLASS_NAME = "TransactionIdempotencyService";


    public void logCreateTransactionalIdempotencyEntry(String transactionId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "createOutboxEntry", "Payment processing started"));
        logData.addAll(LogBuilder.requestLog("POST", REQUEST_FROM));
        logData.add(kv("event", "idempotency.creation.starting"));
        logData.add(kv("transactionId", transactionId));

        log.info("Creating TransactionIdempotency", logData.toArray());
    }

    public void logErrorToCreateTransactionIdempotencyEntry(String transactionId, String errorMessage) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "createOutboxEntry", "Payment processing failed"));
        logData.addAll(LogBuilder.requestLog("POST", REQUEST_FROM));
        logData.add(kv("event", "idempotency.creation.failed"));
        logData.add(kv("transactionId", transactionId));
        logData.add(kv("errorMessage", errorMessage));

        log.error("Error creating TransactionIdempotency Entry", logData.toArray());
    }
}
