package com.matheus.payments.Application.DTOs;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DepositResponse {

    UUID depositId;
    String status;
    LocalDateTime depositTime;

    public DepositResponse(UUID depositId, String status) {
        this.depositId = depositId;
        this.status = status;
        this.depositTime = LocalDateTime.now();
    }
}
