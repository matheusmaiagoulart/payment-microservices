package com.matheus.payments.user_service.Domain.Exceptios;

public class EmailAlreadyExists extends DomainException {
    public EmailAlreadyExists(String message) {
        super(message);
    }
    public EmailAlreadyExists() { super("This E-mail is already registered"); }
}
