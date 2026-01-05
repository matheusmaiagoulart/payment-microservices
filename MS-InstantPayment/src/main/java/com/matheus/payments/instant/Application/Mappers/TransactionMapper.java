package com.matheus.payments.instant.Application.Mappers;

import com.matheus.payments.instant.Application.DTOs.TransactionRequest;
import com.matheus.payments.instant.Domain.Transaction;
import org.shared.DTOs.TransactionDTO;

public interface TransactionMapper {

    TransactionDTO mapToDTO(Transaction transaction);
    Transaction mapToEntity(TransactionRequest transactionRequest);
}
