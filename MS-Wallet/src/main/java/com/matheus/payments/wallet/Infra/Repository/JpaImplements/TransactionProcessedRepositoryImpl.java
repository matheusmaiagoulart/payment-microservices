package com.matheus.payments.wallet.Infra.Repository.JpaImplements;

import com.matheus.payments.wallet.Domain.Models.TransactionsProcessed;
import com.matheus.payments.wallet.Domain.Repositories.TransactionProcessedRepository;
import com.matheus.payments.wallet.Infra.Repository.JpaInterfaces.JpaTransactionProcessedRepository;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionProcessedRepositoryImpl implements TransactionProcessedRepository {

    private final JpaTransactionProcessedRepository jpaTransactionProcessedRepository;

    public TransactionProcessedRepositoryImpl(JpaTransactionProcessedRepository jpaTransactionProcessedRepository) {
        this.jpaTransactionProcessedRepository = jpaTransactionProcessedRepository;
    }

    @Override
    public TransactionsProcessed saveAndFlush(TransactionsProcessed transactionsProcessed) {
        return jpaTransactionProcessedRepository.saveAndFlush(transactionsProcessed);
    }
}

