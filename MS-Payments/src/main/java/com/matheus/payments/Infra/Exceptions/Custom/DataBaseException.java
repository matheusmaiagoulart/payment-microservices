package com.matheus.payments.Infra.Exceptions.Custom;

public class DataBaseException extends RuntimeException {
    public DataBaseException(String message) {
        super(message);
    }
    public DataBaseException() { super(); }
}
