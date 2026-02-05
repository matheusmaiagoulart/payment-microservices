package com.matheus.payments.wallet.Domain.Models;

import com.matheus.payments.wallet.Domain.Exceptions.InsufficientBalanceException;
import com.matheus.payments.wallet.Domain.Exceptions.InvalidAmountException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.shared.Domain.accountType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@NoArgsConstructor
@Getter
@Setter
public class Wallet {

    @Id
    private UUID accountId;

    @NotNull
    private BigDecimal balance;

    @NotNull
    @Enumerated(EnumType.STRING)
    private accountType accountType;

    @NotNull
    private String socialId;

    private Boolean isActive;
    private LocalDateTime createdAt;

    @Version
    private Integer version;

    public Wallet(UUID accountId, accountType accountType, String socialId) {
        this.socialId = socialId;
        this.accountId = accountId;
        this.balance = BigDecimal.ZERO;
        this.accountType = accountType;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public void debitAccount(BigDecimal amount) {

        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP); // Ensure amount has 2 decimal places, and round up if necessary

        if (normalizedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException();
        }
        if (!sufficientBalanceValidation(normalizedAmount)) {
            throw new InsufficientBalanceException();
        }
        this.balance = this.balance.subtract(normalizedAmount).setScale(2, RoundingMode.HALF_UP);
    }

    public void creditAccount(BigDecimal amount) {
        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP); // Ensure amount has 2 decimal places, and round up if necessary
        this.balance = this.balance.add(normalizedAmount).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean sufficientBalanceValidation(BigDecimal amount) {
        // If balance >= amount, return true
        return this.balance.compareTo(amount) >= 0;
    }
}


