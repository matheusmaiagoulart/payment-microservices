package com.matheus.payments.wallet.Infra.Exceptions.Handler;

import com.matheus.payments.wallet.Infra.Exceptions.Custom.InsufficientBalanceException;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.KeyValueAlreadyExists;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.SameUserException;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.WalletNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class WalletExceptionHandler {

    @ExceptionHandler(SameUserException.class)
    private ResponseEntity<HandlerMessage> SameUserException(SameUserException sameUserException){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.BAD_REQUEST , sameUserException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handlerMessage);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    private ResponseEntity<HandlerMessage> WalletNotFound(WalletNotFoundException walletNotFound){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.NOT_FOUND , walletNotFound.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(handlerMessage);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    private ResponseEntity<HandlerMessage> InsufficientBalanceException(InsufficientBalanceException insufficientBalanceException){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.BAD_REQUEST , insufficientBalanceException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handlerMessage);
    }

    @ExceptionHandler(KeyValueAlreadyExists.class)
    private ResponseEntity<HandlerMessage> KeyValueAlreadyExists(KeyValueAlreadyExists keyValueAlreadyExists){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.BAD_REQUEST , keyValueAlreadyExists.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handlerMessage);
    }
}
