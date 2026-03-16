package com.matheus.payments.user_service.Fixtures;

import com.matheus.payments.user_service.Domain.Models.Outbox;

import java.util.UUID;

/**
 * Fixture class for creating Outbox objects for tests.
 */
public class OutboxFixture {

    public static final String CORRELATION_ID = "550e8400-e29b-41d4-a716-446655440000";

    public static Outbox createValidOutbox() {
        return new Outbox(
                UUID.randomUUID(),
                "UserCreated",
                "user-created-topic",
                "{\"userId\":\"123\",\"cpf\":\"12345678901\"}",
                CORRELATION_ID
        );
    }

    public static Outbox createOutboxWithCustomData(UUID userId, String eventType, String topic, String payload) {
        return new Outbox(userId, eventType, topic, payload, CORRELATION_ID);
    }

    public static Outbox createSentOutbox() {
        Outbox outbox = createValidOutbox();
        outbox.setSent(true);
        return outbox;
    }

    public static Outbox createFailedOutbox() {
        Outbox outbox = createValidOutbox();
        outbox.setFailed(true);
        outbox.setFailureReason("Connection timeout");
        return outbox;
    }
}
