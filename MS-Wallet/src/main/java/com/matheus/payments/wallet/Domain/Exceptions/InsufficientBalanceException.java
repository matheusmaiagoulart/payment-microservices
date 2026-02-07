package com.matheus.payments.wallet.Domain.Exceptions;

public class InsufficientBalanceException extends DomainException {
    public static final String CODE = "INSUFFICIENT_FUNDS";

    public InsufficientBalanceException() {
        super(CODE, "Insufficient funds in sender's wallet");
    }
}
