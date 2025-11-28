package com.matheus.payments.wallet.Domain.Wallet;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.UUID;

@Entity
@Table(name = "wallet_keys")
@Getter @Setter
public class WalletKeys {

    @Id
    private UUID id;
    private String keyValue;
    private keyType type;
    private UUID walletId;


    protected WalletKeys() {
    }

    public WalletKeys(String keyValue, keyType type, UUID walletId) {
        this.id = UUID.randomUUID();
        this.keyValue = keyValue;
        this.type = type;
        this.walletId = walletId;
    }
}
