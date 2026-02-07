package com.matheus.payments.wallet.Domain.Exceptions;


public class SameUserException extends DomainException {
    private static final String CODE = "SAME_USER";

    public SameUserException(String message) {
        super(CODE, message);
    }
}
