package com.matheus.payments.wallet.Infra.Exceptions.Custom;

public class TransactionAlreadyProcessed extends RuntimeException {
    public TransactionAlreadyProcessed() {
        super();
    }
}
