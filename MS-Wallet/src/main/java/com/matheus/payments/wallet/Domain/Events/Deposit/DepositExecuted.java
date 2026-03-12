package com.matheus.payments.wallet.Domain.Events.Deposit;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DepositExecuted {

    private UUID depositId;
    private UUID receiverId;
    private BigDecimal amount;
    private boolean successful;
    private LocalDateTime timestamp;

    public DepositExecuted(UUID depositId, UUID userId, BigDecimal amount) {
        this.depositId = depositId;
        this.receiverId = userId;
        this.amount = amount;
        this.successful = true;
        this.timestamp = LocalDateTime.now();
    }
}
