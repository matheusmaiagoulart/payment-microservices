package com.matheus.payments.wallet.Domain.Exceptions;

public class FailedToSaveLedgeEntry extends RuntimeException {
    public FailedToSaveLedgeEntry(String message) {
        super(message);
    }
}
