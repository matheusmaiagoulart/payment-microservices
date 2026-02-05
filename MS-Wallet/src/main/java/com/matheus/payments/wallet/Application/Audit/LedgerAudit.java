package com.matheus.payments.wallet.Application.Audit;

import com.matheus.payments.wallet.utils.ApplicationData;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class LedgerAudit {

    private final String CLASS_NAME = "LedgerService";
    private final String METHOD_NAME = "createLedgerEntries";

    public void logFailedCreateLedgerEntries(String transactionId, String senderKeyValue) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Failed to create ledger entries for transactionId " + transactionId));
        logData.add(kv("senderKyValue", senderKeyValue));
        logData.add(kv("event", "create.ledger.failed"));

        log.error("Failed to create ledger entries" , logData.toArray());
    }
}
