package com.matheus.payments.wallet.Infra.Exceptions.Custom;

public class ConcurrentTransactionException extends RuntimeException {
    public ConcurrentTransactionException() {
        super("Your transaction could not be processed. Please try again in a few moments.");
    }
}
