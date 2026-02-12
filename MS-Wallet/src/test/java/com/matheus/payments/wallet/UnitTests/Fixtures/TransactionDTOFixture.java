package com.matheus.payments.wallet.UnitTests.Fixtures;

import org.shared.DTOs.TransactionDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionDTOFixture {

    public static TransactionDTO createTransactionDTO(BigDecimal amount) {
        return new TransactionDTO(
                UUID.randomUUID(),
                "11111111111",
                "22222222222",
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(),
                amount,
                "PENDING",
                java.time.LocalDateTime.now()
        );
    }

    public static TransactionDTO createTransactionDTO(
            UUID transactionId,
            String senderKey,
            String receiverKey,
            UUID senderAccountId,
            UUID receiverAccountId,
            BigDecimal amount,
            String status,
            LocalDateTime timestamp
    ) {
        return new TransactionDTO(
                transactionId,
                senderKey,
                receiverKey,
                senderAccountId,
                receiverAccountId,
                amount,
                status,
                timestamp
        );
    }
}
