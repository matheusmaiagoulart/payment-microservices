package com.matheus.payments.wallet.Domain.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "deposits_processed")
public class DepositsProcessed {

    @Id
    private UUID depositId;

    private LocalDateTime timestamp;

    public DepositsProcessed(UUID depositId) {
        this.depositId = depositId;
        this.timestamp = LocalDateTime.now();
    }
}
