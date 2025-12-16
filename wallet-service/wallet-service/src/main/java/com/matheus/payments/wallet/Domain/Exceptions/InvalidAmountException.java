package com.matheus.payments.wallet.Domain.Exceptions;


public class InvalidAmountException extends DomainException {
    public InvalidAmountException(String message) { super(message); }
    public InvalidAmountException() { super("The amount provide is invalid!"); }
}
