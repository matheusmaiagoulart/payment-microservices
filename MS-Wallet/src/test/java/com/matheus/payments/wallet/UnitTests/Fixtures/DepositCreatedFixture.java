package com.matheus.payments.wallet.UnitTests.Fixtures;

import com.matheus.payments.wallet.Infra.Kafka.Listeners.DepositCreated.DepositCreated;

import java.math.BigDecimal;
import java.util.UUID;

public class DepositCreatedFixture {

    public static DepositCreated createDepositCreated(BigDecimal amount) {
        return new DepositCreated(
                UUID.randomUUID(),
                UUID.randomUUID(),
                amount
        );
    }

    public static DepositCreated createDepositCreated(UUID depositId, UUID receiverId, BigDecimal amount) {
        return new DepositCreated(
                depositId,
                receiverId,
                amount
        );
    }
}

