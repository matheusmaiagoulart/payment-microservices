package com.matheus.payments.wallet.Infra.Exceptions.Custom;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String message) {
        super(message);
    }
    public WalletNotFoundException() {
        super();
    }
}
