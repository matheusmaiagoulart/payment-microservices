package com.matheus.payments.user_service.Domain.Exceptios;

public class PhoneNumberAlreadyExistsException extends DomainException {

    public static final String CODE = "PHONE_NUMBER_ALREADY_EXISTS";
    public static final String MESSAGE = "This Phone Number is already registered";

    public PhoneNumberAlreadyExistsException() { super(CODE, MESSAGE); }
}
