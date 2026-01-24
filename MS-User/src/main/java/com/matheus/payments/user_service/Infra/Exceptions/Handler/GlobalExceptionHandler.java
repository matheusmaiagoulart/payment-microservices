package com.matheus.payments.user_service.Infra.Exceptions.Handler;

import com.matheus.payments.user_service.Domain.Exceptios.InvalidCpfException;
import com.matheus.payments.user_service.Infra.Exceptions.Custom.OutboxPersistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OutboxPersistException.class)
    public ResponseEntity<HandlerMessage> OutboxPersistException(OutboxPersistException ex) {
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(handlerMessage);
    }
}
