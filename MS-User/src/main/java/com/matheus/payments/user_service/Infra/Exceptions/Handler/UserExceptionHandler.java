package com.matheus.payments.user_service.Infra.Exceptions.Handler;

import com.matheus.payments.user_service.Domain.Exceptios.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(InvalidCpfException.class)
    public ResponseEntity<HandlerMessage> InvalidCpfException(InvalidCpfException ex) {
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.CONFLICT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(handlerMessage);
    }

    @ExceptionHandler(PhoneNumberFormatException.class)
    public ResponseEntity<HandlerMessage> InvalidCpfException(PhoneNumberFormatException ex) {
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handlerMessage);
    }

    @ExceptionHandler(UniqueFieldsEntryViolationException.class)
    public ResponseEntity<HandlerMessage> UniqueFieldsEntryViolationException(UniqueFieldsEntryViolationException ex) {
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handlerMessage);
    }

    @ExceptionHandler(CpfAlreadyExistsException.class)
    public ResponseEntity<HandlerMessage> CpfAlreadyExistsException(CpfAlreadyExistsException ex) {
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.CONFLICT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(handlerMessage);
    }

    @ExceptionHandler(EmailAlreadyExists.class)
    public ResponseEntity<HandlerMessage> EmailAlreadyExists(EmailAlreadyExists ex) {
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.CONFLICT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(handlerMessage);
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<HandlerMessage> PhoneNumberAlreadyExistsException(PhoneNumberAlreadyExistsException ex) {
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.CONFLICT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(handlerMessage);
    }
}
