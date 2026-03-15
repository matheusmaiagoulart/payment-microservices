package com.matheus.payments.user_service.Domain.Exceptios;

public class EmailAlreadyExists extends DomainException {

    public static final String CODE = "EMAIL_ALREADY_EXISTS";
    public static final String MESSAGE = "This E-mail is already registered";

    public EmailAlreadyExists() { super(CODE, MESSAGE); }
}
