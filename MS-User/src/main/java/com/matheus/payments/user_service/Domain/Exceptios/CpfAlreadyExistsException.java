package com.matheus.payments.user_service.Domain.Exceptios;

public class CpfAlreadyExistsException extends DomainException {

    public static final String CODE = "CPF_ALREADY_EXISTS";
    public static final String MESSAGE = "This CPF is already registered";

    public CpfAlreadyExistsException() { super(CODE, MESSAGE); }
}
