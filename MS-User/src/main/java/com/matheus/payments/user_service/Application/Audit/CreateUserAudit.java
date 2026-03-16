package com.matheus.payments.user_service.Application.Audit;

import com.matheus.payments.user_service.Utils.ApplicationData;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class CreateUserAudit {

    private final String CLASS_NAME = "CreateUser";
    private final String METHOD_NAME = "createUser";

    public void logUserCreationStarting(String keyValue) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "Starting User creation"));
        logData.addAll(LogBuilder.requestLog("POST", "/users/create"));
        logData.add(kv("key_value", keyValue));
        logData.add(kv("correlation_id", CorrelationId.get()));
        logData.add(kv("event", "user.creation.starting"));

        log.info("Starting User creation", logData.toArray());
    }

    public void logUserCreationSuccess(String keyValue) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "User creation successfully"));
        logData.addAll(LogBuilder.requestLog("POST", "/users/create"));
        logData.add(kv("key_value", keyValue));
        logData.add(kv("correlation_id", CorrelationId.get()));
        logData.add(kv("event", "user.creation.successfully"));

        log.info("User creation successfully", logData.toArray());

    }

    public void logUserCreationFailed(String keyValue, String errorMessage) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "User creation failed"));
        logData.addAll(LogBuilder.requestLog("POST", "/users/create"));
        logData.add(kv("key_value", keyValue));
        logData.add(kv("correlation_id", CorrelationId.get()));
        logData.add(kv("error_message", errorMessage));
        logData.add(kv("event", "user.creation.failed"));

        log.error("User creation failed", logData.toArray());
    }


    // Audit for user activation


    public void logUserCreatedEventReceived(String keyValue) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, "WalletCreatedListener", "Received WalletCreated event for key:" + keyValue));
        logData.addAll(LogBuilder.eventLog("WalletCreated", "wallet-created", keyValue));
        logData.add(kv("correlation_id", CorrelationId.get()));
        logData.add(kv("event", "wallet_created.event.received"));

        log.info("Received UserCreated event for key: " + keyValue, logData.toArray());
    }

    public void logUserActivated(String keyValue) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, "executeActivationUserAccount", "The account was activated for key:" + keyValue));
        logData.addAll(LogBuilder.eventLog("WalletCreated", "wallet-created", keyValue));
        logData.add(kv("correlation_id", CorrelationId.get()));
        logData.add(kv("event", "wallet_created.event.activated"));

        log.info("The account was activated for key: " + keyValue, logData.toArray());
    }

    public void logUserActivatedError(String keyValue, String errorMessage) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, "executeActivationUserAccount", "An error occurred during account activation for user key:" + keyValue));
        logData.addAll(LogBuilder.eventLog("WalletCreated", "wallet-created", keyValue));
        logData.add(kv("correlation_id", CorrelationId.get()));
        logData.add(kv("error_message", errorMessage));
        logData.add(kv("event", "wallet_created.event.activation.failed"));

        log.error("An error occurred during account activation for user key: " + keyValue, logData.toArray());
    }
}
