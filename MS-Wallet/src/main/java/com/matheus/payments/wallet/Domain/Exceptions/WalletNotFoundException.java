package com.matheus.payments.wallet.Domain.Exceptions;

public class WalletNotFoundException extends DomainException {
    public WalletNotFoundException(String message) {
        super(message);
    }
    public WalletNotFoundException() {
        super();
    }
}
