package com.matheus.payments.instant.Application.Mappers.Transaction;

import com.matheus.payments.instant.Application.DTOs.TransactionDTO;
import com.matheus.payments.instant.Application.DTOs.TransactionRequest;
import com.matheus.payments.instant.Domain.Transaction.Transaction;

public interface TransactionMapper {

    TransactionDTO mapToDTO(Transaction transaction);
    Transaction mapToEntity(TransactionRequest transactionRequest);
}
