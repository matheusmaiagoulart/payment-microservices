package com.matheus.payments.wallet.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@NoArgsConstructor
@Getter @Setter
public class Wallet {

    @Id
    private UUID accountId;
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    private accountType accountType;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public Wallet(accountType accountType) {
        this.accountId = UUID.randomUUID();
        this.balance = BigDecimal.ZERO;
        this.accountType = accountType;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public void debitAccount(BigDecimal amount){
        this.balance = this.balance.subtract(amount);
    }

    public void creditAccount(BigDecimal amount){
        this.balance = this.balance.add(amount);
    }
}


