package com.matheus.payments.wallet.Infra.Audit;

import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserCreatedListenerAudit {

    private final String CLASS_NAME = "UserCreatedListener";
    private final String METHOD_NAME = "createUserWalletListener";
    private final String MICROSERVICE_NAME = "wallet-service";
    private final String TOPIC_NAME = "UserCreated";
    private final String EVENT_NAME = "UserCreated";

    public void logUserCreatedEventReceived(String keyValue) {
        log.info("Received UserCreated event for key: " + keyValue,
                LogBuilder.eventLog(EVENT_NAME, TOPIC_NAME, MICROSERVICE_NAME, keyValue, CLASS_NAME, METHOD_NAME,
                        "user_created.event.received"));
    }

    public void logMessageProcessed(String keyValue) {
        log.info("Successfully processed UserCreated event for key: " + keyValue,
                LogBuilder.eventLog(EVENT_NAME, TOPIC_NAME, MICROSERVICE_NAME, keyValue, CLASS_NAME, METHOD_NAME,
                        "user_created.event.processed"));
    }

    public void logFailedToProcessMessage(String keyValue , String errorMessage) {
        log.error("Failed to process UserCreated event for key: " + keyValue,
                LogBuilder.eventLog(EVENT_NAME, TOPIC_NAME, MICROSERVICE_NAME, keyValue, CLASS_NAME, METHOD_NAME,
                        "user_created.event.processing.failed"), errorMessage);
    }

}
