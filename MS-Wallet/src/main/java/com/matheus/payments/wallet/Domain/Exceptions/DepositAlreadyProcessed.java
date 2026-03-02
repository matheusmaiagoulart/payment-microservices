package com.matheus.payments.wallet.Domain.Exceptions;

public class DepositAlreadyProcessed extends DomainException {

    public static final String CODE = "DEPOSIT_ALREADY_PROCESSED";
    public static final String MESSAGE = "This deposit has already been processed.";

        public DepositAlreadyProcessed() {
        super(CODE, MESSAGE);
    }
}
