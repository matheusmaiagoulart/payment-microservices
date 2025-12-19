package com.matheus.payments.wallet.Domain;

import com.matheus.payments.wallet.Domain.Exceptions.InsufficientBalanceException;
import com.matheus.payments.wallet.Domain.Exceptions.InvalidAmountException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private BigDecimal balance;

    @NotNull
    @Enumerated(EnumType.STRING)
    private accountType accountType;

    private Boolean isActive;
    private LocalDateTime createdAt;

    @Version
    private Integer version;

    public Wallet(accountType accountType) {
        this.accountId = UUID.randomUUID();
        this.balance = BigDecimal.ZERO;
        this.accountType = accountType;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public void debitAccount(BigDecimal amount) {
        
        if(amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException();
        }
        if(!sufficientBalanceValidation(amount)) {
            throw new InsufficientBalanceException();
        }

        this.balance = this.balance.subtract(amount);
    }

    public void creditAccount(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public boolean sufficientBalanceValidation(BigDecimal amount) {
        // If balance >= amount, return true
        return this.balance.compareTo(amount) >= 0;
    }
}


