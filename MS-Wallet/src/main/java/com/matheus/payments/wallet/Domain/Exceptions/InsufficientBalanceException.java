package com.matheus.payments.wallet.Domain.Exceptions;

public class InsufficientBalanceException extends DomainException {
    public InsufficientBalanceException() {
        super("Insufficient funds in sender's wallet");
    }
}
