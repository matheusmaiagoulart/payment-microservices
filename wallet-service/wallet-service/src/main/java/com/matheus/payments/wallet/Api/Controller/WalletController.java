package com.matheus.payments.wallet.Api.Controller;

import com.matheus.payments.wallet.Application.DTOs.Request.CreateWalletRequest;
import com.matheus.payments.wallet.Application.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/create")
    public HttpStatus createWallet(@RequestBody CreateWalletRequest request) {

        var result = walletService.createWallet(request.getUserId(), request.getAccountType());
        return result;
    }
}
