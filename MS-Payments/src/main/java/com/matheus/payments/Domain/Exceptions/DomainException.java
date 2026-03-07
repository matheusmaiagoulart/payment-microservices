package com.matheus.payments.Domain.Exceptions;

public class DomainException extends RuntimeException {
    public final String errorCode;

    public DomainException(String errorCode, String MESSAGE) {
        super(MESSAGE);
        this.errorCode = errorCode;
    }

    public String getCODE() {
        return errorCode;
    }

}
