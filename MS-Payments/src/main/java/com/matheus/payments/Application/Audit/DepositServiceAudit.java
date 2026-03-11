package com.matheus.payments.Application.Audit;

import com.matheus.payments.Utils.ApplicationData;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class DepositServiceAudit {

    public static final String CLASS_NAME = "DepositService";
    public static final String ENDPOINT_ENTRY_POINT = "transaction/deposit";

    public void logCreateDepositEntry() {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "createDeposit", "Deposit processing starting"));
        logData.addAll(LogBuilder.requestLog("POST", ENDPOINT_ENTRY_POINT));
        logData.add(kv("event", "deposit.create.start"));

        log.info("Creating deposit request", logData.toArray());
    }

    public void logErrorCreateDeposit(String message) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "createDeposit", "Deposit processing failed"));
        logData.add(kv("event", "transaction.create.error"));
        logData.add(kv("error_message", message));

        log.error("Error creating Deposit", logData.toArray());
    }

}