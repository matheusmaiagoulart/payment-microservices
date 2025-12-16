package org.shared.Logs;

/**
 * Using LogBuilder to create structured methods to logging in the application
 * 
 * The methods are used to create logs with less code repetition
 *
 * @author Matheus Maia Goulart
 */
public class Log {


    // Service Logs
    public static Object[] logServiceInstantPayment(String transactionId, String className, String methodName, String message) {
        return LogBuilder.serviceLog("/transaction/pix", "Instant-Payment", transactionId, className, methodName, message);
    }


    // Request Logs
    public static Object[] logRequestWalletInstantPayment(String transactionId, String className, String methodName) {
        return LogBuilder.requestLog("POST", "/wallets/instant-payment", "Wallet", transactionId, className, methodName);
    }
    public static Object[] logRequestWalletInstantPayment(String transactionId, String className, String methodName, Object... additionalKVs) {
        return LogBuilder.requestLog("POST", "/wallets/instant-payment", "Wallet", transactionId, className, methodName, additionalKVs);
    }

}
