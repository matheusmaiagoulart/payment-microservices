package com.matheus.payments.Application.Mappers;

import com.matheus.payments.Application.DTOs.TransactionRequest;
import com.matheus.payments.Domain.Transaction;
import org.shared.DTOs.TransactionDTO;

public interface TransactionMapper {

    TransactionDTO mapToDTO(Transaction transaction);
    Transaction mapToEntity(TransactionRequest transactionRequest);
}
