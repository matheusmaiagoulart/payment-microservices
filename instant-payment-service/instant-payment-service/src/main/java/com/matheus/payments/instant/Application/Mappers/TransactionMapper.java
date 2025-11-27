package com.matheus.payments.instant.Application.Mappers;

import com.matheus.payments.instant.Application.DTOs.Response.TransactionDTO;
import com.matheus.payments.instant.Application.DTOs.Request.TransactionRequest;
import com.matheus.payments.instant.Domain.Transaction;

public interface TransactionMapper {

    TransactionDTO mapToDTO(Transaction transaction);
    Transaction mapToEntity(TransactionRequest transactionRequest);
}
