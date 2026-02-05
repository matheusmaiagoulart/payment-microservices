package com.matheus.payments.wallet.Application.DTOs.Context;

import com.matheus.payments.wallet.Domain.Models.PixKey;
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
        PixKey senderPixKey;
        PixKey receiverPixKey;
        BigDecimal amount;

    public PixTransfer(String transactionId, PixKey senderPixKey, PixKey receiverPixKey, BigDecimal amount) {
        this.transactionId = UUID.fromString(transactionId);
        this.senderPixKey = senderPixKey;
        this.receiverPixKey = receiverPixKey;
        this.amount = amount;
    }
}
