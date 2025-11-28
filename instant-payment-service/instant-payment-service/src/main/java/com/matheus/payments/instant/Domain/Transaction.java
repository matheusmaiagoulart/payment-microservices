package com.matheus.payments.instant.Domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "transactions")
@NoArgsConstructor
@Getter @Setter
public class Transaction {

    @Id
    private UUID transactionId;

    private String senderKey;
    private String receiverKey;

    private UUID senderAccountId;
    private UUID receiverAccountId;

    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime timestamp;

    public Transaction(String senderKey, String receiverKey, BigDecimal amount)
    {
        this.transactionId = UUID.randomUUID();
        this.receiverKey = receiverKey;
        this.senderKey = senderKey;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
    }

}
