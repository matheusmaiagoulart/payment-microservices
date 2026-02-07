package com.matheus.payments.wallet.Domain.Exceptions;

public class WalletNotFoundException extends DomainException {

    public static final String SENDER_CODE = "SENDER_WALLET_NOT_FOUND";
    public static final String RECEIVER_CODE = "RECEIVER_WALLET_NOT_FOUND";

    public WalletNotFoundException(String CODE, String message) {
        super(CODE, message);
    }

    public static WalletNotFoundException senderNotFound(){
        return new WalletNotFoundException(SENDER_CODE, "Sender wallet not found.");
    }

    public static WalletNotFoundException receiverNotFound(){
        return new WalletNotFoundException(RECEIVER_CODE, "Receiver wallet not found.");
    }
}
