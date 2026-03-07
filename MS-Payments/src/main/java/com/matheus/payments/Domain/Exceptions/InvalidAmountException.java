package com.matheus.payments.Domain.Exceptions;

public class InvalidAmountException extends DomainException {

    public final static String CODE = "INVALID_AMOUNT";
    public final static String MESSAGE = "The amount must be greater than zero.";

    public InvalidAmountException() {
        super(CODE, MESSAGE);
    }
}
