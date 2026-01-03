package com.matheus.payments.user_service.Domain.Exceptios;

public class InvalidCpfException extends DomainException {
    public InvalidCpfException() {
        super("Invalid CPF provided. Must contain 11 digits.");
    }
    public InvalidCpfException(String message) {
        super(message);
    }
}
