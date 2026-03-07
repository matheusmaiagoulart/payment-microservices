package com.matheus.payments.Infra.Repository.JpaImplements;

import com.matheus.payments.Domain.Models.Transaction;
import com.matheus.payments.Domain.Repositories.TransactionRepository;
import com.matheus.payments.Infra.Repository.JpaInterfaces.JpaTransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionRepositoryImpl implements TransactionRepository {

    private final JpaTransactionRepository transactionJpaRepository;

    public TransactionRepositoryImpl(JpaTransactionRepository transactionJpaRepository) {
        this.transactionJpaRepository = transactionJpaRepository;
    }

    @Override
    public void save(Transaction transaction) {
        transactionJpaRepository.save(transaction);
    }

    @Override
    public Optional<Transaction> findByTransactionId(UUID transactionId) {
        return transactionJpaRepository.findByTransactionId(transactionId);
    }

    @Override
    public Optional<List<Transaction>> findTransactionsBySenderAccountIdOrReceiverAccountId(UUID senderAccountId, UUID receiverAccountId) {
        return transactionJpaRepository.findTransactionsBySenderAccountIdOrReceiverAccountId(senderAccountId, receiverAccountId);
    }
}
