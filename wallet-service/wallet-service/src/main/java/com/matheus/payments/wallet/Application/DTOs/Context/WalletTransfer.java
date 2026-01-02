package com.matheus.payments.wallet.Application.DTOs.Context;

import com.matheus.payments.wallet.Domain.Models.Wallet;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * This class was created to encapsulate the data required for a wallet transfer operation.
 * When using Optimistic Locking, it's essential to have all necessary information bundled together
 * to ensure data consistency and integrity during concurrent transactions.
 *
 */
@Getter
public class WalletTransfer {

    private Wallet senderWallet;
    private Wallet receiverWallet;
    private BigDecimal amount;

    public WalletTransfer(Wallet senderWallet, Wallet receiverWallet, BigDecimal amount) {
        this.senderWallet = senderWallet;
        this.receiverWallet = receiverWallet;
        this.amount = amount;
    }
}
