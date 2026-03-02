package com.matheus.payments.wallet.Application.Audit;

import com.matheus.payments.wallet.utils.ApplicationData;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class DepositAudit {

    private final String CLASS_NAME = "Deposit";
    private final String METHOD_NAME = "executeDeposit";


    public void logEventReceived() {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Message received to execute deposit"));
        logData.add(kv("correlationId", CorrelationId.get()));

        log.info("Message received to execute deposit", logData.toArray());
    }

    public void logStartDepositExecution(UUID depositId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Starting deposit process"));
        logData.add(kv("depositId", depositId));
        logData.add(kv("correlationId", CorrelationId.get()));

        log.info("Starting deposit process", logData.toArray());
    }

    public void logDepositExecuted(UUID depositId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Deposit executed successfully"));
        logData.add(kv("depositId", depositId));
        logData.add(kv("correlationId", CorrelationId.get()));

        log.info("Deposit executed successfully", logData.toArray());
    }

    public void logDepositFailed(UUID depositId, String reason) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Deposit execution failed"));
        logData.add(kv("depositId", depositId));
        logData.add(kv("correlationId", CorrelationId.get()));
        logData.add(kv("Failure Reason: ", reason));

        log.error("Deposit execution failed", logData.toArray());
    }
}
