package com.matheus.payments.user_service.Application.Audit;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CorrelationId {


    public static String generate() {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        return correlationId;
    }

    public static void set(String correlationId) {
        MDC.put("correlationId", correlationId);
    }

    public static String get() {
        return MDC.get("correlationId");
    }

    public static void clear() {
        MDC.remove("correlationId");
    }
}
