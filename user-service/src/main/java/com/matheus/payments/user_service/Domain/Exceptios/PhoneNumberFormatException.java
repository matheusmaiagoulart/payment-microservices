package com.matheus.payments.user_service.Domain.Exceptios;

public class PhoneNumberFormatException extends DomainException {
    public PhoneNumberFormatException() { super("Phone number must have 11 digits"); }
}
