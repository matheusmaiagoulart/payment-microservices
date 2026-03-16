package com.matheus.payments.Application.Audit;

import com.matheus.payments.Infra.Exceptions.Custom.FailedToSentException;
import com.matheus.payments.Utils.ApplicationData;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Audit class for logging events related to payment processing with the Wallet Server.
 *
 * @author Matheus Maia Goulart
 */

@Slf4j
@Component
public class PaymentProcessorAudit {

    public static final String WALLET_ENDPOINT = "/wallets/instant-payment";
    public static final String WALLET_SERVICE_NAME = "MS-Wallet";
    public static final String CLASS_NAME = "PaymentProcessorService";

    public void logSendingRequestWallet(String transactionId) {

        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "sendPaymentToProcessor", "Sending payment to Wallet Server"));
        logData.addAll(LogBuilder.requestLog("POST", WALLET_ENDPOINT));
        logData.add(kv("target_service", WALLET_SERVICE_NAME));
        logData.add(kv("event", "payment.request.sending"));
        logData.add(kv("transactionId", transactionId));

        log.info("Sending payment to Wallet Server", logData.toArray());
    }

    public void logErrorSendingRequestWallet(String transactionId, FailedToSentException errorMessage) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "sendPaymentToProcessor", "Error to sent a request for Wallet Server"));
        logData.addAll(LogBuilder.requestLog("POST", WALLET_ENDPOINT));
        logData.add(kv("target_service", WALLET_SERVICE_NAME));
        logData.add(kv("event", "payment.request.send.failed"));
        logData.add(kv("transactionId", transactionId));
        logData.add(kv("errorMessage", errorMessage.getMessage()));

        log.error("Error to sent a request for Wallet Server", logData.toArray());
    }

    public void logErrorToParseResponseInformations(String transactionId, String response, String errorMessage) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "sendPaymentToProcessor", "Error to parse response informations from Wallet Server"));
        logData.addAll(LogBuilder.requestLog("POST", WALLET_ENDPOINT));
        logData.add(kv("target_service", WALLET_SERVICE_NAME));
        logData.add(kv("event", "payment.request.send.success.response.parse.failed"));
        logData.add(kv("transactionId", transactionId));
        logData.add(kv("response", response));
        logData.add(kv("errorMessage", errorMessage));

        log.error("Error to parse response", logData.toArray());
    }

    public void logSentSuccessfullyWallet(String transactionId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "sendPaymentToProcessor", "Successfully sent payment to Wallet Server"));
        logData.addAll(LogBuilder.requestLog("POST", WALLET_ENDPOINT));
        logData.add(kv("target_service", WALLET_SERVICE_NAME));
        logData.add(kv("event", "payment.request.sent.success"));
        logData.add(kv("transactionId", transactionId));

        log.info("Successfully sent payment to Wallet Server", logData.toArray());
    }

    public void logReceivedSuccessResponse(String transactionId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "paymentStatusUpdate", "Payment response received from Wallet Server was successfully"));
        logData.addAll(LogBuilder.requestLog("POST", WALLET_ENDPOINT));
        logData.add(kv("target_service", WALLET_SERVICE_NAME));
        logData.add(kv("event", "wallet.response.received.success"));
        logData.add(kv("transactionId", transactionId));

        log.info("Payment response received from Wallet Server was successfully", logData.toArray());
    }

    public void logReceivedFailedResponse(String transactionId, String failureReason) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "paymentStatusUpdate", "Payment response received from Wallet Server was failed"));
        logData.addAll(LogBuilder.requestLog("POST", WALLET_ENDPOINT));
        logData.add(kv("target_service", WALLET_SERVICE_NAME));
        logData.add(kv("event", "wallet.response.received.failed"));
        logData.add(kv("transactionId", transactionId));
        logData.add(kv("failureReason", failureReason));

        log.warn("Payment response received from Wallet Server was failed", logData.toArray());
    }

    public void logReceivedNotSentResponse(String transactionId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.SERVICE_NAME, CorrelationId.get(), CLASS_NAME, "paymentStatusUpdate", "An error occurred while sending payment to Wallet Server"));
        logData.addAll(LogBuilder.requestLog("POST", WALLET_ENDPOINT));
        logData.add(kv("target_service", WALLET_SERVICE_NAME));
        logData.add(kv("event", "wallet.request.not.sent"));
        logData.add(kv("transactionId", transactionId));

        log.warn("An error occurred while sending payment to Wallet Server", logData.toArray());

    }

}
