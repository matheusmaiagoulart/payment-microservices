package com.matheus.payments.instant.Application.Audit;

import com.matheus.payments.instant.Utils.ApplicationData;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Audit class is responsible for create the logs related to InstantPaymentFacade.
 * <p>
 * This class provide methods to build structured logs with fixed data and dynamic data using parameters.
 *
 * @author Matheus Maia Goulart
 */
@Slf4j
@Component
public class InstantPaymentFacadeAudit {

    private static final String WALLET_ENDPOINT = "/wallets/instant-payment";
    private static final String CLASS_NAME = "InstantPaymentFacade";

    public void logPaymentProcessStarting(String transactionId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "sendPaymentToProcessor", "sendPaymentToProcessor"));
        logData.addAll(LogBuilder.requestLog("POST", WALLET_ENDPOINT));
        logData.add(kv("event", "payment.process.starting"));
        logData.add(kv("transactionId", transactionId));

        log.info("Sending payment to Wallet Server", logData.toArray());
    }
}
