package com.matheus.payments.instant.Infra.Exceptions.Custom;

public class TransactionFailedException extends RuntimeException{
    public TransactionFailedException(){
        super();
    }
    public TransactionFailedException(String message){
        super(message);
    }
}
