package com.matheus.payments.wallet.Domain.Exceptions;

public class SocialIdAlreadyExistsException extends DomainException {
    public SocialIdAlreadyExistsException(String message) { super(message); }
    public SocialIdAlreadyExistsException() { super(); }
}
