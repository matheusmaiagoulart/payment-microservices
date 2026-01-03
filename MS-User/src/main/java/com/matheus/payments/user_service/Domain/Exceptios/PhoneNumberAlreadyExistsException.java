package com.matheus.payments.user_service.Domain.Exceptios;

public class PhoneNumberAlreadyExistsException extends DomainException {
    public PhoneNumberAlreadyExistsException(String message) {
        super(message);
    }
    public PhoneNumberAlreadyExistsException() { super("This Phone Number is already registered"); }
}
