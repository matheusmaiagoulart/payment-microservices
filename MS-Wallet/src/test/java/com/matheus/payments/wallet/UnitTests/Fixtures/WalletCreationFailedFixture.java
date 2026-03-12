package com.matheus.payments.wallet.UnitTests.Fixtures;

import com.matheus.payments.wallet.Domain.Events.CreateWallet.WalletCreationFailed;

import java.util.UUID;

public class WalletCreationFailedFixture {

    public static WalletCreationFailed createWalletCreationFailed() {
        return new WalletCreationFailed(
                UUID.randomUUID(),
                "11111111111",
                "Error creating wallet"
        );
    }

    public static WalletCreationFailed createWalletCreationFailed(UUID userId, String cpf, String errorMessage) {
        return new WalletCreationFailed(userId, cpf, errorMessage);
    }
}

