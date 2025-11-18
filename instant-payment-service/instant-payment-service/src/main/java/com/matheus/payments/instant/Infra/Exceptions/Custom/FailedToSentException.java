package com.matheus.payments.instant.Infra.Exceptions.Custom;

public class FailedToSentException extends RuntimeException{

    public FailedToSentException(){
        super();
    }
    public FailedToSentException(String message){
        super(message);
    }
}
