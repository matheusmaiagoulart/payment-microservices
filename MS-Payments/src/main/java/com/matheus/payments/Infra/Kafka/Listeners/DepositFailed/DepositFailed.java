package com.matheus.payments.Infra.Kafka.Listeners.DepositFailed;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DepositFailed {

    private UUID depositId;
    private UUID receiverId;
    private BigDecimal amount;
    private boolean successful;
    private String failureReason;
    private LocalDateTime timestamp;

    public DepositFailed(UUID depositId, boolean successful, UUID userId, BigDecimal amount, String failureReason) {
        this.depositId = depositId;
        this.receiverId = userId;
        this.amount = amount;
        this.successful = successful;
        this.failureReason = failureReason;
        this.timestamp = LocalDateTime.now();
    }
}
