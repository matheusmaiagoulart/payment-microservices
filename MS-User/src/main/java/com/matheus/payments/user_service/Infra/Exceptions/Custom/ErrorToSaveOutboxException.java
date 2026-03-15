package com.matheus.payments.user_service.Infra.Exceptions.Custom;

public class ErrorToSaveOutboxException extends RuntimeException {

    public static final String MESSAGE = "Error to save outbox message";

    public ErrorToSaveOutboxException() { super(MESSAGE); }
}