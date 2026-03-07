package com.matheus.payments.Domain.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@NoArgsConstructor
@Getter @Setter
@Entity
@Table(name = "transactions")

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

    public void setTransactionCompleted(UUID senderAccountId, UUID receiverAccountId){
        this.status = TransactionStatus.COMPLETED;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
    }

    public void setTransactionFailed(){
        this.status = TransactionStatus.FAILED;
    }

}
