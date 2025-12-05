package com.matheus.payments.wallet.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "wallet_keys")
@Getter @Setter
public class WalletKeys {

    @Id
    private UUID id;
    @Column(name = "key_value")
    private String keyValue;
    private keyType type;
    private UUID accountId;


    protected WalletKeys() {
    }

    public WalletKeys(String keyValue, keyType type, UUID walletId) {
        this.id = UUID.randomUUID();
        this.keyValue = keyValue;
        this.type = type;
        this.accountId = walletId;
    }
}
