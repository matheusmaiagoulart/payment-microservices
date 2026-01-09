package com.matheus.payments.wallet.Domain.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.shared.Domain.keyType;

import java.util.UUID;

@Entity
@Table(name = "wallet_keys")
@Getter @Setter
public class PixKey {

    @Id
    private UUID id;
    @Column(name = "key_value")
    private String keyValue;
    private keyType type;
    private UUID accountId;


    protected PixKey() {
    }

    public PixKey(String keyValue, keyType type, UUID walletId) {
        this.id = UUID.randomUUID();
        this.keyValue = keyValue;
        this.type = type;
        this.accountId = walletId;
    }
}
