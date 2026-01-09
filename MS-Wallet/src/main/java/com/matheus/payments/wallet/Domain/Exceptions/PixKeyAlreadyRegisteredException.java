package com.matheus.payments.wallet.Domain.Exceptions;

public class PixKeyAlreadyRegisteredException extends DomainException {
    public PixKeyAlreadyRegisteredException(String message) { super(message); }
    public PixKeyAlreadyRegisteredException() { super(); }
}
