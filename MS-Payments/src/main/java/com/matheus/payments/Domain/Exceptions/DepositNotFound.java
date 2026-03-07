package com.matheus.payments.Domain.Exceptions;

public class DepositNotFound extends DomainException {

    public static final String CODE = "DEPOSIT_NOT_FOUND";
    public static final String MESSAGE = "Deposit not found for id: %s";

    public DepositNotFound(String depositId) {
        super(CODE, String.format(MESSAGE, depositId));
    }
}
