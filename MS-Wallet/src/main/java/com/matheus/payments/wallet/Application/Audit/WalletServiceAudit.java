package com.matheus.payments.wallet.Application.Audit;

import com.matheus.payments.wallet.utils.ApplicationData;
import com.matheus.payments.wallet.utils.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class WalletServiceAudit {

    private final String CLASS_NAME = "WalletService";
    private final String METHOD_NAME = "transferProcess";
    private final String WALLET_ENDPOINT = "/wallets/instant-payment";

    // Transfer Process Logs
    public void logStartingTransferProcess(String transactionId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Starting transfer process"));
        logData.addAll(LogBuilder.requestLog("POST", "/wallets/instant-payment"));
        logData.add(kv("transactionId", transactionId));

        log.info("Starting transfer process", logData.toArray());
    }

    public void logBalanceValidation(String transactionId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Validating balance operation"));
        logData.add(kv("transactionId", transactionId));

        log.info("Validating balance", logData.toArray());
    }

    public void logTransferSuccess(String transactionId) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Transfer completed successfully"));
        logData.add(kv("transactionId", transactionId));
        logData.add(kv("event", "transfer.process.success"));

        log.info("Transfer completed successfully", logData.toArray());
    }

    public void logTransferError(String transactionId, String errorMessage) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Transfer failed"));
        logData.add(kv("transactionId", transactionId));
        logData.add(kv("errorMessage", errorMessage));
        logData.add(kv("event", "transfer.process.failed"));

        log.error("Transfer failed", logData.toArray());
    }


    // Create Wallet Logs

    public void logCreatingWallet(String keyValue) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Creating wallet for user: " + keyValue));
        logData.addAll(LogBuilder.requestLog("POST", WALLET_ENDPOINT));
        logData.addAll(LogBuilder.eventLog("UserCreated", KafkaTopics.USER_CREATED_EVENT_TOPIC, keyValue));
        logData.add(kv("keyValue", keyValue));
        logData.add(kv("event", "create.wallet.process.start"));

        log.info("Creating wallet for user: " + keyValue, logData.toArray());
    }

    public void logFailedCreateWallet(String keyValue) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "The key value already exists: " + keyValue));
        logData.addAll(LogBuilder.requestLog("POST", WALLET_ENDPOINT));
        logData.addAll(LogBuilder.eventLog("UserCreated", KafkaTopics.USER_CREATED_EVENT_TOPIC, keyValue));
        logData.add(kv("keyValue", keyValue));
        logData.add(kv("event", "create.wallet.process.failed"));

        log.error("The key value already exists: " + keyValue, logData.toArray());
    }

    public void logFailedGeneric(String keyValue, String message) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll( LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, message + keyValue));
        logData.addAll(LogBuilder.requestLog("POST", WALLET_ENDPOINT));
        logData.addAll(LogBuilder.eventLog("UserCreated", KafkaTopics.USER_CREATED_EVENT_TOPIC, keyValue));
        logData.add(kv("keyValue", keyValue));
        logData.add(kv("event", "create.wallet.process.failed"));

        log.warn(message + keyValue, logData.toArray());
    }


}
