package com.matheus.payments.instant.Application.Audit;

import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.Log;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class TransactionServiceAudit {

    private static final String CLASS_NAME = "TransactionService";

    public void logCreateTransaction(String transactionId) {
        log.info("Creating Transaction",
                Log.logServiceInstantPayment(transactionId, CLASS_NAME, "createPaymentProcess", "Payment processing starting"));
    }

    public void logErrorCreateTransaction(String message) {
        log.error("Error creating Transaction",
                Log.logServiceInstantPayment("", CLASS_NAME, "createPaymentProcess", "Payment processing failed"),
                kv("ErrorMessage", message));
    }



}
