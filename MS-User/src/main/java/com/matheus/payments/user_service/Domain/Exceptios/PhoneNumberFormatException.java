package com.matheus.payments.user_service.Domain.Exceptios;

public class PhoneNumberFormatException extends DomainException {

    public static final String CODE = "PHONE_NUMBER_FORMAT_ERROR";
    public static final String MESSAGE = "Phone number must have 11 digits.";

    public PhoneNumberFormatException() { super(CODE, MESSAGE); }
}
