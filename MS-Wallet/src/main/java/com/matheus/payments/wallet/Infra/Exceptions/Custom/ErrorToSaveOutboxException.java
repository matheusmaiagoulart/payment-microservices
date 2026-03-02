package com.matheus.payments.wallet.Infra.Exceptions.Custom;

public class ErrorToSaveOutboxException extends RuntimeException {

    public static final String MESSAGE = "Error to save outbox message";

    public ErrorToSaveOutboxException() {
        super(MESSAGE);
    }
}
