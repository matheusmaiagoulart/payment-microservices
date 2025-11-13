package com.matheus.payments.instant.Application.Mappers.Transaction;

import com.matheus.payments.instant.Application.DTOs.TransactionDTO;
import com.matheus.payments.instant.Application.DTOs.TransactionRequest;
import com.matheus.payments.instant.Domain.Transaction.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapperImpl implements TransactionMapper {


    @Override
    public TransactionDTO mapToDTO(Transaction transaction) {
        return new TransactionDTO(
                transaction.getTransactionId(),
                transaction.getSenderId(),
                transaction.getReceiverId(),
                transaction.getAmount(),
                transaction.getProcessed(),
                transaction.getTimestamp()
        );
    }


    @Override
    public Transaction mapToEntity(TransactionRequest transactionRequest) {
        return new Transaction(
                transactionRequest.getSenderId(),
                transactionRequest.getReceiverId(),
                transactionRequest.getAmount()
        );
    }
}
