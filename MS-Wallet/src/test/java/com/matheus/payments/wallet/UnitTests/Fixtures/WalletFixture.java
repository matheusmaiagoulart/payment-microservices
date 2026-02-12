package com.matheus.payments.wallet.UnitTests.Fixtures;

import com.matheus.payments.wallet.Domain.Models.Wallet;
import org.shared.Domain.accountType;

import java.util.UUID;

public class WalletFixture {

    public static Wallet createWallet() {
        return new Wallet(
                UUID.randomUUID(),
                accountType.CHECKING,
                "11111111111" );
    }

    public static Wallet createWallet(
            UUID accountId,
            accountType accountType,
            String socialId
    ) {
        return new Wallet(
                accountId,
                accountType,
                socialId );
    }
}
