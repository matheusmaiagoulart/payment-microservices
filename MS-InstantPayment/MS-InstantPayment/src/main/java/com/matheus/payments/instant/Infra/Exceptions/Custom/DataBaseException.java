package com.matheus.payments.instant.Infra.Exceptions.Custom;

public class DataBaseException extends RuntimeException {
    public DataBaseException(String message) {
        super(message);
    }
    public DataBaseException() { super(); }
}
