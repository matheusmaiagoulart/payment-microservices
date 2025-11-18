package com.matheus.payments.wallet.Infra.Exceptions.Custom;


public class SameUserException extends RuntimeException {
    public SameUserException() {
        super();
    }
    public SameUserException(String message) {
        super(message);
    }
}
