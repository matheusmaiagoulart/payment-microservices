package org.shared.Logs;

import java.util.Arrays;

import static net.logstash.logback.argument.StructuredArguments.kv;

public class LogBuilder {

    public static Object[] serviceLog(String requestURL, String MS, String transactionId, String className, String methodName, String message) {
        return new Object[]{
                kv("requestURL", requestURL),
                kv("MS", MS),
                kv("transactionId", transactionId),
                kv("class", className),
                kv("method", methodName),
                kv("event", message)
        };
    }

    public static Object[] requestLog(String httpMethod, String endpoint, String MS, String transactionId, String className, String methodName, Object... additionalKVs) {
        var objBase = new Object[]{
                kv("http_method", httpMethod),
                kv("endpoint", endpoint),
                kv("MS", MS),
                kv("transactionId", transactionId),
                kv("class", className),
                kv("method", methodName)
        };

        Object[] log = new Object[objBase.length + additionalKVs.length];

        // obj base, index start copy, destination, index start array final, length array base
        System.arraycopy(objBase, 0, log, 0, objBase.length);
        System.arraycopy(additionalKVs, 0, log, objBase.length, additionalKVs.length);

        return log;
    }
}
