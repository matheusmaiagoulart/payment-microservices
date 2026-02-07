package com.matheus.payments.wallet.Domain.Exceptions;


public class InvalidAmountException extends DomainException {
    public static final String CODE = "INVALID_AMOUNT";

    public InvalidAmountException() {
        super(CODE, "The amount provide is invalid! Must be a positive value.");
    }
}
