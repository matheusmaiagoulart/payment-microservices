package com.matheus.payments.wallet.Infra.Exceptions.Custom;

public class FailedToSaveLedgeEntry extends RuntimeException {
    public FailedToSaveLedgeEntry(String message) {
        super(message);
    }
}
