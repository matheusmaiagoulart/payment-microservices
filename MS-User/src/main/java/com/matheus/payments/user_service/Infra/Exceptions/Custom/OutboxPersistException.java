package com.matheus.payments.user_service.Infra.Exceptions.Custom;

public class OutboxPersistException extends RuntimeException {
    public OutboxPersistException(String message) {
        super(message);
    }
    public OutboxPersistException(){ super("An error occurred while saving Outbox"); }
}
