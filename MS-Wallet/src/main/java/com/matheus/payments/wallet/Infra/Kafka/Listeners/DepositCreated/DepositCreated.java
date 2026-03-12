package com.matheus.payments.wallet.Infra.Kafka.Listeners.DepositCreated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositCreated {

    private UUID depositId;
    private UUID receiverId;
    private BigDecimal amount;
}
