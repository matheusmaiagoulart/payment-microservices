package com.matheus.payments.Infra.Exceptions.Custom;

public class CreatePaymentProcessException extends RuntimeException {

    public CreatePaymentProcessException() { super(); }
    public CreatePaymentProcessException(String message) {
        super(message);
    }
}
