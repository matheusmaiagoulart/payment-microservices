package com.matheus.payments.Domain.Events;

import com.matheus.payments.Domain.Models.Deposit;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class DepositCreatedEvent {

    private final UUID depositId;
    private final UUID receiverId;
    private final BigDecimal amount;

    public DepositCreatedEvent(Deposit deposit) {
        this.depositId = deposit.getDepositId();
        this.receiverId = deposit.getReceiverId();
        this.amount = deposit.getAmount();
    }
}
