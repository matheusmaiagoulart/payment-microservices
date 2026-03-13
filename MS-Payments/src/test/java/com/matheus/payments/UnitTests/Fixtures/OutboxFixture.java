package com.matheus.payments.UnitTests.Fixtures;

import com.matheus.payments.Domain.Models.TransactionOutbox;

import java.util.UUID;

/**
 * Fixture class for Outbox-related test data.
 */
public class OutboxFixture {

    public static final String DEFAULT_PAYLOAD = "{\"test\":\"data\"}";
    public static final String DEFAULT_TOPIC = "test-topic";

    public static TransactionOutbox createOutbox() {
        return new TransactionOutbox(
                UUID.randomUUID().toString(),
                DEFAULT_PAYLOAD,
                UUID.randomUUID().toString(),
                DEFAULT_TOPIC
        );
    }

    public static TransactionOutbox createOutbox(String transactionId, String payload, String topic) {
        return new TransactionOutbox(
                transactionId,
                payload,
                UUID.randomUUID().toString(),
                topic
        );
    }
}

