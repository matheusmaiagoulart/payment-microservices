package com.matheus.payments.user_service.Domain.Exceptios;

public class DomainException extends RuntimeException {

    private final String CODE;
    
    public DomainException(String CODE, String MESSAGE) {
        super(MESSAGE);
        this.CODE = CODE;
    }
}
