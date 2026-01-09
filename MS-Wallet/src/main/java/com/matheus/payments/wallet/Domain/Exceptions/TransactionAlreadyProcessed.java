package com.matheus.payments.wallet.Domain.Exceptions;

public class TransactionAlreadyProcessed extends DomainException {
    public TransactionAlreadyProcessed() {
        super("Transaction has already been processed.");
    }
}
