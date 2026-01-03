package com.matheus.payments.user_service.Domain.Exceptios;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
