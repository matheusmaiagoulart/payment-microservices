package com.matheus.payments.instant.Application.Audit;

import com.matheus.payments.instant.Infra.Exceptions.Custom.FailedToSentException;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

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
    public static final String WALLET_SERVICE_NAME = "Wallet-Service";
    public static final String CLASS_NAME = "PaymentProcessorService";

    public void logSendingRequestWallet(String transactionId) {
        log.info("Sending payment to Wallet Server",
                LogBuilder.requestLog("POST", WALLET_ENDPOINT, WALLET_SERVICE_NAME, transactionId, CLASS_NAME, "sendPaymentToProcessor",
                        kv("event", "payment.request.sending")));
    }
    public void logErrorSendingRequestWallet(String transactionId, FailedToSentException errorMessage) {
        log.warn("Error to sent a request for Wallet Server",
                LogBuilder.requestLog("POST", WALLET_ENDPOINT, WALLET_SERVICE_NAME, transactionId, CLASS_NAME, "sendPaymentToProcessor",
                        kv("event", "payment.request.send.failed"),
                        kv("errorMessage", errorMessage.getMessage())));
    }

    public void logSentSuccessfullyWallet(String transactionId) {
        log.info("Successfully sent payment to Wallet Server",
                LogBuilder.requestLog("POST", WALLET_ENDPOINT, WALLET_SERVICE_NAME, transactionId, CLASS_NAME, "sendPaymentToProcessor",
                        kv("event", "payment.request.sent.success")));
    }

    public void logReceiveResponse(String transactionId) {
        log.info("Payment response received from Wallet Server",
                LogBuilder.requestLog("POST", WALLET_ENDPOINT, WALLET_SERVICE_NAME, transactionId, CLASS_NAME, "paymentStatusUpdate",
                        kv("event", "payment.response.received")));
    }

    public void logReceivedSuccessResponse(String transactionId) {
        log.info("Payment response received from Wallet Server was successfully",
                LogBuilder.requestLog("POST", WALLET_ENDPOINT, WALLET_SERVICE_NAME, transactionId, CLASS_NAME, "paymentStatusUpdate",
                        kv("event", "wallet.response.received.success")));
    }

    public void logReceivedFailedResponse(String transactionId, String failureReason) {
        log.warn("Payment response received from Wallet Server was failed",
                LogBuilder.requestLog("POST", WALLET_ENDPOINT, WALLET_SERVICE_NAME, transactionId, CLASS_NAME, "paymentStatusUpdate",
                        kv("event", "wallet.response.received.failed"),
                        kv("failureReason", failureReason)));
    }

}
