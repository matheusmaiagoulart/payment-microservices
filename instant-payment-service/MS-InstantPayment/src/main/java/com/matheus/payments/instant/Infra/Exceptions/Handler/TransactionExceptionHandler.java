package com.matheus.payments.instant.Infra.Exceptions.Handler;

import com.matheus.payments.instant.Infra.Exceptions.Custom.*;
import com.matheus.payments.instant.Infra.Exceptions.HandlerMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TransactionExceptionHandler {

    @ExceptionHandler(TransactionAlreadySentException.class)
    public ResponseEntity<HandlerMessage> TransactionAlreadySentException(TransactionAlreadySentException transactionException){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.CONFLICT, transactionException.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(handlerMessage);
    }

    @ExceptionHandler(FailedToSentException.class)
    public ResponseEntity<HandlerMessage> FailedToSentException(FailedToSentException failedToSentException){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.INTERNAL_SERVER_ERROR, failedToSentException.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(handlerMessage);
    }

    @ExceptionHandler(TransactionFailedException.class)
    public ResponseEntity<HandlerMessage> TransactionFailed(TransactionFailedException transactionFailedException) {
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.BAD_REQUEST, transactionFailedException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handlerMessage);
    }

    @ExceptionHandler(TransactionNotFound.class)
    public ResponseEntity<HandlerMessage> TransactionNotFound(TransactionNotFound transactionNotFound) {
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.NOT_FOUND, transactionNotFound.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(handlerMessage);
    }
}
