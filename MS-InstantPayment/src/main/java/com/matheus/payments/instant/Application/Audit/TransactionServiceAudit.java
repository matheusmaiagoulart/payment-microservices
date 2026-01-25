package com.matheus.payments.instant.Application.Audit;

import com.matheus.payments.instant.Utils.ApplicationData;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class TransactionServiceAudit {

    private static final String CLASS_NAME = "TransactionService";
    private static final String ENDPOINT_INSTANT_PAYMENT = "/transaction/pix";

    public void logCreateTransaction(String methodName) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, methodName, "Payment processing starting"));
        logData.addAll(LogBuilder.requestLog("POST", ENDPOINT_INSTANT_PAYMENT));
        logData.add(kv("event", "transaction.create.start"));

        log.info("Creating Transaction", logData.toArray());
    }

    public void logErrorCreateTransaction(String message) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "createPaymentProcess", "Payment processing failed"));
        logData.add(kv("event", "transaction.create.error"));
        logData.add(kv("error_message", message));

        log.error("Error creating Transaction", logData.toArray());
    }


}
