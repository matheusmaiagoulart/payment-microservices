package com.matheus.payments.wallet.Domain.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "deposits_processed")
public class DepositsProcessed {

    @Id
    private UUID depositId;

    public DepositsProcessed(UUID depositId) {
        this.depositId = depositId;
    }
}
