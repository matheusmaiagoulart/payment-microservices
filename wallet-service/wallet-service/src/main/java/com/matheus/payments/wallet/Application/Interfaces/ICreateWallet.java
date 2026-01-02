package com.matheus.payments.wallet.Application.Interfaces;

import com.matheus.payments.wallet.Application.DTOs.Request.CreateWalletRequest;

public interface ICreateWallet {
    boolean createWallet(CreateWalletRequest request);
}
