package com.matheus.payments.wallet.Infra.Exceptions.Custom;

public class InsuficientBalanceException extends RuntimeException {
    public InsuficientBalanceException(String message) {
        super(message);
    }

    public InsuficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
