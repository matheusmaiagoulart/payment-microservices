package com.matheus.payments.instant.Infra.Exceptions.Handler;

import com.matheus.payments.instant.Infra.Exceptions.Custom.DataBaseException;
import com.matheus.payments.instant.Infra.Exceptions.HandlerMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class DataBaseExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<HandlerMessage> SQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException sqlIntegrityConstraintViolationException) {
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.CONFLICT, "Database constraint violation: " + sqlIntegrityConstraintViolationException.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(handlerMessage);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<HandlerMessage> TransactionSystemException(TransactionSystemException transactionSystemException) {
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.CONFLICT, "Transaction Failed: " + transactionSystemException.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(handlerMessage);
    }

    @ExceptionHandler(DataBaseException.class)
    public ResponseEntity<HandlerMessage> DataBaseException(DataBaseException dataBaseException) {
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.SERVICE_UNAVAILABLE, dataBaseException.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(handlerMessage);
    }
}
