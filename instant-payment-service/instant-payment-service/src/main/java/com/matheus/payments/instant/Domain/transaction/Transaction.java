package com.matheus.payments.instant.Domain.transaction;

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
    private UUID TransactionId;

    private UUID senderId;
    private UUID receiverId;
    private BigDecimal amount;
    private Boolean processed;
    private LocalDateTime timestamp;

    public Transaction(UUID senderId, UUID receiverId, BigDecimal amount)
    {
        this.TransactionId = UUID.randomUUID();
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.processed = false;
    }

}
