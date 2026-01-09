package com.matheus.payments.wallet.Domain.Exceptions;

/**
 * This Exception is used to indicate domain-specific errors in the application.
 */
public class DomainException extends RuntimeException{

    public DomainException() {
        super();
    }
    public DomainException(String message) {
        super(message);
    }

}
