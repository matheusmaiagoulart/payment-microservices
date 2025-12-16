package com.matheus.payments.wallet.Application.DTOs.Context;

import com.matheus.payments.wallet.Domain.Wallet;
import com.matheus.payments.wallet.Domain.PixKey;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Context class is responsible for holding all necessary information to process a Pix transfer between two wallets.
 *
 * @author Matheus Maia Goulart
 */

@Getter
@Setter
public class PixTransfer {
        UUID transactionId;
        Wallet senderWallet;
        Wallet receiverWallet;
        PixKey senderPixKey;
        PixKey receiverPixKey;
        BigDecimal amount;

    public PixTransfer(String transactionId, Wallet senderWallet, Wallet receiverWallet, PixKey senderPixKey, PixKey receiverPixKey, BigDecimal amount) {
        this.transactionId = UUID.fromString(transactionId);
        this.senderWallet = senderWallet;
        this.receiverWallet = receiverWallet;
        this.senderPixKey = senderPixKey;
        this.receiverPixKey = receiverPixKey;
        this.amount = amount;
    }
}
