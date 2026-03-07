package com.matheus.payments.Domain.Exceptions;

public class TransactionFailedException extends DomainException {

    public static final String ERROR_CODE = "TRANSACTION_FAILED";
    public static final String MESSAGE = "The transaction has failed. Reason: ";

    public TransactionFailedException(String message) {
        super(ERROR_CODE, MESSAGE.concat(message));
    }
}
