package com.matheus.payments.wallet.Application.DTOs.Context;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TransferResult {

    public enum Status {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        SENDER_WALLET_NOT_FOUND,
        RECEIVER_WALLET_NOT_FOUND,
        ERROR
    }

    private UUID transactionId;
    private Status status;
    private String message;


    public TransferResult(UUID transactionId, Status status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }

    public boolean isSuccess() {
        return this.status == Status.SUCCESS;
    }
}