package com.matheus.payments.wallet.UnitTests.Fixtures;

import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Domain.Models.PixKey;
import org.shared.DTOs.TransactionDTO;

public class PixTransferFixture {

    public static PixTransfer createPixTransfer(TransactionDTO request, PixKey senderPixKey, PixKey receiverPixKey) {
        return new PixTransfer(
                request.getTransactionId(),
                senderPixKey,
                receiverPixKey,
                request.getAmount()
        );
    }
}
