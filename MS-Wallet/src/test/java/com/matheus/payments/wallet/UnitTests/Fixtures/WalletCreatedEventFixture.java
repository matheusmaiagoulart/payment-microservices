package com.matheus.payments.wallet.UnitTests.Fixtures;

import com.matheus.payments.wallet.Domain.Events.CreateWallet.WalletCreatedEvent;

import java.util.UUID;

public class WalletCreatedEventFixture {

    public static WalletCreatedEvent createWalletCreatedEvent() {
        return new WalletCreatedEvent(
                UUID.randomUUID(),
                "11111111111"
        );
    }

    public static WalletCreatedEvent createWalletCreatedEvent(UUID userId, String cpf) {
        return new WalletCreatedEvent(userId, cpf);
    }
}

