package com.matheus.payments.wallet.Infra.Exceptions.Handler;

import com.matheus.payments.wallet.Domain.Exceptions.*;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class WalletExceptionHandler {

    @ExceptionHandler(SameUserException.class)
    public ResponseEntity<HandlerMessage> SameUserException(SameUserException sameUserException){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.BAD_REQUEST , sameUserException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handlerMessage);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<HandlerMessage> WalletNotFound(WalletNotFoundException walletNotFound){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.NOT_FOUND , (walletNotFound.getMessage() + " wallet not found or inactive"));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(handlerMessage);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<HandlerMessage> InsufficientBalanceException(InsufficientBalanceException insufficientBalanceException){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.BAD_REQUEST , insufficientBalanceException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handlerMessage);
    }

    @ExceptionHandler(PixKeyAlreadyRegisteredException.class)
    public ResponseEntity<HandlerMessage> KeyValueAlreadyExists(PixKeyAlreadyRegisteredException keyValueAlreadyExists){
    HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.BAD_REQUEST , "Key value already exists: " + keyValueAlreadyExists.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handlerMessage);
    }

    @ExceptionHandler(TransactionAlreadyProcessed.class)
    public ResponseEntity<HandlerMessage> TransactionAlreadyProcessed(TransactionAlreadyProcessed transactionAlreadyProcessed){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.CONFLICT , transactionAlreadyProcessed.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(handlerMessage);
    }

    @ExceptionHandler(FailedToSaveLedgeEntry.class)
    public ResponseEntity<HandlerMessage> FailedToSave(FailedToSaveLedgeEntry failedToSave){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.CONFLICT , "An error occurred while creating ledger entries: " + failedToSave.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(handlerMessage);
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<HandlerMessage> InvalidAmountException(InvalidAmountException invalidAmountException){
        HandlerMessage handlerMessage = new HandlerMessage(HttpStatus.BAD_REQUEST , invalidAmountException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(handlerMessage);
    }
}
