package org.shared.Logs;


import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

public class LogBuilder {

    public static List<Object> baseLog(
            String MS,
            String correlationId,
            String className,
            String methodName,
            String message) {
        return List.of(
                kv("service_name", MS),
                kv("correlation_id", correlationId),
                kv("class", className),
                kv("method", methodName),
                kv("event", message)
        );
    }

    public static List<Object> eventLog
            (String event,
             String topic,
             String keyValue) {
        return List.of(
                kv("event_type", event),
                kv("topic_listened", topic),
                kv("key_value", keyValue));
    }

    public static List<Object> requestLog
            (String httpMethod,
            String endpoint) {
        return List.of(
                kv("http_method", httpMethod),
                kv("target_endpoint", endpoint));
    }
}
