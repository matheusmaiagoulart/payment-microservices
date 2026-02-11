package com.matheus.payments.wallet.Domain.Exceptions;


public class InvalidAmountException extends DomainException {
    public static final String CODE = "INVALID_AMOUNT";
    public static final String ERROR_MESSAGE = "The amount provide is invalid! Must be a positive value.";

    public InvalidAmountException() {
        super(CODE, ERROR_MESSAGE);
    }
}
