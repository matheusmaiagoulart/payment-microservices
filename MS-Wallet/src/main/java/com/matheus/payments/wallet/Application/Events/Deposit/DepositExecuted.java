package com.matheus.payments.wallet.Application.Events.Deposit;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DepositExecuted {

    private UUID receiverId;
    private BigDecimal amount;
    private boolean successful;
    private LocalDateTime timestamp;

    public DepositExecuted(UUID userId, BigDecimal amount) {
        this.receiverId = userId;
        this.amount = amount;
        this.successful = true;
        this.timestamp = LocalDateTime.now();
    }
}
