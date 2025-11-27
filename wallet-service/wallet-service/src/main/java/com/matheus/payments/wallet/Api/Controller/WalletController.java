package com.matheus.payments.wallet.Api.Controller;

import com.matheus.payments.wallet.Application.DTOs.Request.CreateWalletRequest;
import com.matheus.payments.wallet.Application.DTOs.Request.TransactionDTO;
import com.matheus.payments.wallet.Application.DTOs.Response.InstantPaymentResponse;
import com.matheus.payments.wallet.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.wallet.Application.PaymentProcessorService;
import com.matheus.payments.wallet.Application.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private PaymentProcessorService paymentProcessorService;

    @PostMapping("/create")
    public HttpStatus createWallet(@RequestBody CreateWalletRequest request) {

        var result = walletService.createWallet(request.getUserId(), request.getAccountType());
        return result;
    }

    @PostMapping("/instant-payment")
    public PaymentProcessorResponse instantPayment(@RequestBody TransactionDTO request) {
        return walletService.handlePaymentProcessor(request);
    }
}
