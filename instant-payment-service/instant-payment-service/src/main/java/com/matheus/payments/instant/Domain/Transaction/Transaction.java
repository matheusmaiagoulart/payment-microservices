package com.matheus.payments.instant.Domain.Transaction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "transactions")
@NoArgsConstructor
@Getter @Setter
public class Transaction {


    @Id
    private UUID transactionId;

    private UUID senderId;
    private UUID receiverId;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime timestamp;

    public Transaction(UUID senderId, UUID receiverId, BigDecimal amount)
    {
        this.transactionId = UUID.randomUUID();
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
    }

}
