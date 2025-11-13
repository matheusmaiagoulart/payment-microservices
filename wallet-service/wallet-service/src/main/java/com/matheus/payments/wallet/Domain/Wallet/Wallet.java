package com.matheus.payments.wallet.Domain.Wallet;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

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
    private UUID userId;
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    private accountType accountType;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public Wallet(UUID userId, accountType accountType) {
        this.accountId = UUID.randomUUID();
        this.userId = userId;
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


