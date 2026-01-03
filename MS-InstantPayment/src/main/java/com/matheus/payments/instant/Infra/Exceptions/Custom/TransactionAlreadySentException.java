package com.matheus.payments.instant.Infra.Exceptions.Custom;

public class TransactionAlreadySentException extends RuntimeException{

    public TransactionAlreadySentException(){
        super();
    }
    public TransactionAlreadySentException(String message){
        super(message);
    }
}
