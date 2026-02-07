package com.matheus.payments.wallet.Domain.Exceptions;

public class SocialIdAlreadyExistsException extends DomainException {

    private static final String CODE = "SOCIAL_ID_ALREADY_EXISTS";

    public SocialIdAlreadyExistsException(String message) {
        super(CODE, message);
    }

}
