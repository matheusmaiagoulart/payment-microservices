package com.matheus.payments.user_service.Domain.Exceptios;

public class InvalidCpfException extends DomainException {

    public static final String CODE = "CPF_INVALID";
    public static final String MESSAGE = "Invalid CPF provided. Must contain 11 digits.";

    public InvalidCpfException() {
        super(CODE, MESSAGE);
    }
}
