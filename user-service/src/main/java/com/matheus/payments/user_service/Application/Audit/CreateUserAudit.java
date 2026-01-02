package com.matheus.payments.user_service.Application.Audit;

import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class CreateUserAudit {

    private final String CLASS_NAME = "CreateUser";
    private final String METHOD_NAME = "createUser";
    private final String MICROSERVICE_NAME = "user-service";

    public void logUserCreationStarting(String keyValue) {
    log.info("Starting User creation", LogBuilder.requestLog("POST", "/users/create", MICROSERVICE_NAME, null, CLASS_NAME, METHOD_NAME,
            kv("key_value", keyValue),
            kv("event", "user.creation.starting")));
    }

    public void logUserCreationSuccess(String keyValue) {
    log.info("User creation successful", LogBuilder.requestLog("POST", "/users/create", MICROSERVICE_NAME, null, CLASS_NAME, METHOD_NAME,
            kv("key_value", keyValue),
            kv("event", "user.creation.success")));
    }

    public void logUserCreationFailed(String keyValue, String errorMessage) {
    log.error("User creation failed", LogBuilder.requestLog("POST", "/users/create", MICROSERVICE_NAME, null, CLASS_NAME, METHOD_NAME,
            kv("key_value", keyValue),
            kv("event", "user.creation.failed")), errorMessage);
    }
}
