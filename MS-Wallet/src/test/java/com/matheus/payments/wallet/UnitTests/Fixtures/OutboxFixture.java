package com.matheus.payments.wallet.UnitTests.Fixtures;

import com.matheus.payments.wallet.Domain.Models.Outbox;
import com.matheus.payments.wallet.utils.KafkaTopics;

import java.util.UUID;

public class OutboxFixture {

    public static Outbox createOutbox() {
        return new Outbox(UUID.randomUUID(),UUID.randomUUID().toString(), "EventTest", KafkaTopics.WALLET_CREATED_EVENT_TOPIC, "{\"transactionId\":\"" + UUID.randomUUID() + "\", \"amount\": 100.0}");
    }
}
