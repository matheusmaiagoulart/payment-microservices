package com.matheus.payments.wallet.Api.Controller;

import com.matheus.payments.wallet.Application.DTOs.Request.CreateWalletRequest;
import com.matheus.payments.wallet.Application.DTOs.Response.InstantPaymentResponse;
import com.matheus.payments.wallet.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.wallet.Application.Services.WalletService;
import org.shared.DTOs.TransactionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {this.walletService = walletService; }

    @PostMapping("/createAccount")
    public ResponseEntity<String> createWallet(@RequestBody CreateWalletRequest request) {

        var result = walletService.createWallet(request);

        if (!result) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wallet already exists for this user.");
        return ResponseEntity.status(HttpStatus.CREATED).body("Wallet created successfully.");
    }

    @PostMapping("/instant-payment")
    public PaymentProcessorResponse instantPayment(@RequestBody TransactionDTO request) {

        InstantPaymentResponse result = walletService.transferProcess(request);

        if(result.isSucessful()){
            return PaymentProcessorResponse.successResponse(UUID.fromString(request.getTransactionId()), result.getSenderAccountId(), result.getReceiverAccountId());
        } else {
            return PaymentProcessorResponse.failedResponse(UUID.fromString(request.getTransactionId()), result.getSenderAccountId(), result.getReceiverAccountId(), result.getFailedMessage());
        }
    }
}