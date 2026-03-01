package com.matheus.payments.wallet.Application.Events.Deposit;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DepositFailed {

    private UUID receiverId;
    private BigDecimal amount;
    private boolean successful;
    private String failureReason;
    private LocalDateTime timestamp;

    public DepositFailed(boolean successful, UUID userId, BigDecimal amount, String failureReason) {
        this.receiverId = userId;
        this.amount = amount;
        this.successful = successful;
        this.failureReason = failureReason;
        this.timestamp = LocalDateTime.now();
    }
}
