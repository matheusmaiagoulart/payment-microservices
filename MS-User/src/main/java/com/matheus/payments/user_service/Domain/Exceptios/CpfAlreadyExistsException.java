package com.matheus.payments.user_service.Domain.Exceptios;

public class CpfAlreadyExistsException extends DomainException {
    public CpfAlreadyExistsException(String message) {
        super(message);
    }
    public CpfAlreadyExistsException() { super("This CPF is already registered"); }
}
