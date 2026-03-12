package com.matheus.payments.Domain.Models;

import com.matheus.payments.Domain.Exceptions.InvalidAmountException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "deposits")
@NoArgsConstructor
public class Deposit {

    @Id
    private UUID depositId;
    private UUID receiverId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private DepositStatus status;

    private LocalDateTime payedAt;
    private LocalDateTime confirmedAt;

    public Deposit(UUID receiverId, BigDecimal amount) {
        this.depositId = UUID.randomUUID();
        this.receiverId = receiverId;
        this.amount = normalizeAmount(amount);
        this.status = DepositStatus.PENDING;
        this.payedAt = LocalDateTime.now();
        this.confirmedAt = null;
    }

    public enum DepositStatus {
        PENDING,
        SENT,
        CONFIRMED,
        FAILED
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
        return amount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public void updateStatus(DepositStatus status) {
        if (status.equals(DepositStatus.SENT)) {
            this.status = DepositStatus.SENT;
        }
        this.status = status;
        this.confirmedAt = LocalDateTime.now();
    }
}
