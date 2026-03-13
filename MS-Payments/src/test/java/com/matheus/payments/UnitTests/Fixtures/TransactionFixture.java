package com.matheus.payments.UnitTests.Fixtures;

import com.matheus.payments.Application.DTOs.TransactionRequest;
import com.matheus.payments.Domain.Models.Transaction;
import com.matheus.payments.Domain.Models.TransactionIdempotency;
import org.shared.DTOs.PaymentProcessorResponse;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Fixture class for Transaction-related test data.
 */
public class TransactionFixture {

    public static final String SENDER_KEY = "sender@test.com";
    public static final String RECEIVER_KEY = "receiver@test.com";
    public static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("100.00");

    public static Transaction createTransaction() {
        return new Transaction(SENDER_KEY, RECEIVER_KEY, DEFAULT_AMOUNT);
    }

    public static Transaction createTransaction(String senderKey, String receiverKey, BigDecimal amount) {
        return new Transaction(senderKey, receiverKey, amount);
    }

    public static TransactionRequest createTransactionRequest() {
        return createTransactionRequest(SENDER_KEY, RECEIVER_KEY, DEFAULT_AMOUNT);
    }

    public static TransactionRequest createTransactionRequest(String senderKey, String receiverKey, BigDecimal amount) {
        try {
            TransactionRequest request = TransactionRequest.class.getDeclaredConstructor().newInstance();
            setField(request, "senderKey", senderKey);
            setField(request, "receiverKey", receiverKey);
            setField(request, "amount", amount);
            return request;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create TransactionRequest", e);
        }
    }

    public static TransactionIdempotency createTransactionIdempotency(UUID transactionId, String payload) {
        return new TransactionIdempotency(transactionId, payload, UUID.randomUUID().toString());
    }

    private static void setField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}

