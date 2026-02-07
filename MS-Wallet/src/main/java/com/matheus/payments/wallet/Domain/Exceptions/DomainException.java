package com.matheus.payments.wallet.Domain.Exceptions;

/**
 * This Exception is used to indicate domain-specific errors in the application.
 */
public class DomainException extends RuntimeException{

    private final String errorCode;

    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
