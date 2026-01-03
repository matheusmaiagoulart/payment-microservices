package com.matheus.payments.instant.Application.Mappers;

import com.matheus.payments.instant.Application.DTOs.Request.TransactionRequest;
import com.matheus.payments.instant.Domain.Transaction;
import org.shared.DTOs.TransactionDTO;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapperImpl implements TransactionMapper {


    @Override
    public TransactionDTO mapToDTO(Transaction transaction) {
        return new TransactionDTO(
                transaction.getTransactionId(),
                transaction.getSenderKey(),
                transaction.getReceiverKey(),
                transaction.getSenderAccountId(),
                transaction.getReceiverAccountId(),
                transaction.getAmount(),
                transaction.getStatus().toString(),
                transaction.getTimestamp()
        );
    }

    @Override
    public Transaction mapToEntity(TransactionRequest transactionRequest) {
        return new Transaction(
                transactionRequest.getSenderKey(),
                transactionRequest.getReceiverKey(),
                transactionRequest.getAmount()
        );
    }
}
