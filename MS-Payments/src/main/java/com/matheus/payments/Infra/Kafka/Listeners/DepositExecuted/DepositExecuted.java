package com.matheus.payments.Infra.Kafka.Listeners.DepositExecuted;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DepositExecuted {

    private UUID depositId;
    private UUID receiverId;
    private BigDecimal amount;
    private boolean successful;
    private LocalDateTime timestamp;
}
