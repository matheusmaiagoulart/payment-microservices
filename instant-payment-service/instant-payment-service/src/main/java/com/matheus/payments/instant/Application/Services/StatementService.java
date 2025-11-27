package com.matheus.payments.instant.Application.Services;

import com.matheus.payments.instant.Domain.Transaction;
import com.matheus.payments.instant.Infra.Repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.stream;

@Service
public class StatementService {

    private final TransactionRepository transactionRepository;
    public StatementService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getAllTransactionsStatements(UUID accountId){
        return transactionRepository.findTransactionsBySenderAccountIdOrReceiverAccountId(accountId, accountId)
                .orElseThrow(() -> new RuntimeException("No transactions found for accountId: " + accountId));
    }


}
