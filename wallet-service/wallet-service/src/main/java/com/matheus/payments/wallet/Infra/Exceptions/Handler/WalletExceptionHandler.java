package com.matheus.payments.wallet.Infra.Exceptions.Handler;

import com.matheus.payments.wallet.Infra.Exceptions.Custom.InsuficientBalanceException;
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

    @ExceptionHandler(InsuficientBalanceException.class)
    private ResponseEntity<HandlerMessage> InsuficientBalanceException(InsuficientBalanceException insuficientBalanceException){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.BAD_REQUEST , insuficientBalanceException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handlerMessage);
    }
}
