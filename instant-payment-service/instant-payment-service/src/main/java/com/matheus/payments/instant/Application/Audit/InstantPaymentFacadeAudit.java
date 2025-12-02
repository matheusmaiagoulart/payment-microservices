package com.matheus.payments.instant.Application.Audit;

import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

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
    private static final String MICROSERVICE_NAME = "Instant-Payment-Service";
    private static final String CLASS_NAME = "InstantPaymentFacade";

    public void logPaymentProcessStarting(String transactionId) {
        log.info("Sending payment to Wallet Server",
                LogBuilder.requestLog("POST", WALLET_ENDPOINT, MICROSERVICE_NAME, transactionId, CLASS_NAME, "sendPaymentToProcessor",
                        kv("event", "payment.request.sending")));
    }
}
