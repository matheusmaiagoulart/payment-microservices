package com.matheus.payments.wallet.UnitTests.Fixtures;

import com.matheus.payments.wallet.Domain.Models.PixKey;
import org.shared.Domain.keyType;

public class PixKeyFixture {

    public static PixKey createPixKey() {
        return new PixKey(
                "11111111111",
                keyType.CPF,
                java.util.UUID.randomUUID());
    }

    public static PixKey createPixKey(
            String keyValue,
            keyType keyType,
            java.util.UUID walletId
    ) {
        return new PixKey(
                keyValue,
                keyType,
                walletId);
    }

}
