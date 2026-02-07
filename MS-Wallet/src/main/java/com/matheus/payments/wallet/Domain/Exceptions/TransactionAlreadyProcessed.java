package com.matheus.payments.wallet.Domain.Exceptions;

public class TransactionAlreadyProcessed extends DomainException {
    private static final String CODE = "TRANSACTION_ALREADY_PROCESSED";

    public TransactionAlreadyProcessed() {
        super(CODE, "Transaction has already been processed.");
    }
}
