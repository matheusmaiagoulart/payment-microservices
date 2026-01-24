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
        logData.add(kv("event", "user.creation.starting"));

        log.info("Starting User creation", logData.toArray());
    }

    public void logUserCreationSuccess(String keyValue) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "User creation successfully"));
        logData.addAll(LogBuilder.requestLog("POST", "/users/create"));
        logData.add(kv("key_value", keyValue));
        logData.add(kv("event", "user.creation.successfully"));

        log.info("User creation successfully", logData.toArray());

    }

    public void logUserCreationFailed(String keyValue, String errorMessage) {
        ArrayList<Object> logData = new ArrayList<>();

        logData.addAll(LogBuilder.baseLog(ApplicationData.APPLICATION_NAME, CorrelationId.get(), CLASS_NAME, METHOD_NAME, "User creation failed"));
        logData.addAll(LogBuilder.requestLog("POST", "/users/create"));
        logData.add(kv("key_value", keyValue));
        logData.add(kv("error_message", errorMessage));
        logData.add(kv("event", "user.creation.failed"));

        log.error("User creation failed", logData.toArray());
    }
}
