package com.matheus.payments.wallet.Domain.Exceptions;


public class SameUserException extends DomainException {
    public SameUserException() {
        super();
    }
    public SameUserException(String message) {
        super(message);
    }
}
