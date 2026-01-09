package com.matheus.payments.wallet.Api.Controller;

import com.matheus.payments.wallet.Domain.Events.UserCreatedEvent;
import com.matheus.payments.wallet.Application.DTOs.Response.InstantPaymentResponse;
import com.matheus.payments.wallet.Application.UseCases.CreateWallet;
import com.matheus.payments.wallet.Application.UseCases.InstantPayment;
import org.shared.DTOs.PaymentProcessorResponse;
import org.shared.DTOs.TransactionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final InstantPayment instantPayment;
    private final CreateWallet createWallet;

    public WalletController(InstantPayment instantPayment, CreateWallet createWallet) {
        this.instantPayment = instantPayment;
        this.createWallet = createWallet;
    }

    @PostMapping("/createAccount")
    public ResponseEntity<String> createWallet(@RequestBody UserCreatedEvent request) {

        var result = createWallet.createWallet(request);

        if (!result) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wallet already exists for this user.");
        return ResponseEntity.status(HttpStatus.CREATED).body("Wallet created successfully.");
    }

    @PostMapping("/instant-payment")
    public PaymentProcessorResponse instantPayment(@RequestBody TransactionDTO request) {

        InstantPaymentResponse result = instantPayment.transferProcess(request);

        if (!result.isSucessful()) {
            return PaymentProcessorResponse.failedResponse(false, UUID.fromString(request.getTransactionId()), result.getSenderAccountId(), result.getReceiverAccountId(), result.getFailedMessage());
        }
        else if (result.isAlreadyProcessed()) {
            return PaymentProcessorResponse.failedResponse(true, UUID.fromString(request.getTransactionId()), result.getSenderAccountId(), result.getReceiverAccountId(), result.getFailedMessage());
        }

        return PaymentProcessorResponse.successResponse(UUID.fromString(request.getTransactionId()), result.getSenderAccountId(), result.getReceiverAccountId());
    }
}