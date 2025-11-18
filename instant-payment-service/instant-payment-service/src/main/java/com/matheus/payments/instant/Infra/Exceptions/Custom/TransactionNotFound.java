package com.matheus.payments.instant.Infra.Exceptions.Custom;

public class TransactionNotFound extends RuntimeException {
    public TransactionNotFound() {
        super();
    }
    public TransactionNotFound(String message) {
        super(message);
    }
}
