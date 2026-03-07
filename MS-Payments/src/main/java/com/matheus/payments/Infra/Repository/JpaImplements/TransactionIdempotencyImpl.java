package com.matheus.payments.Infra.Repository.JpaImplements;

import com.matheus.payments.Domain.Models.TransactionIdempotency;
import com.matheus.payments.Domain.Repositories.TransactionIdempotencyRepository;
import com.matheus.payments.Infra.Repository.JpaInterfaces.JpaTransactionIdempotency;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionIdempotencyImpl implements TransactionIdempotencyRepository {

    private final JpaTransactionIdempotency jpaTransactionIdempotencyRepository;

    public TransactionIdempotencyImpl(JpaTransactionIdempotency jpaTransactionIdempotencyRepository) {
        this.jpaTransactionIdempotencyRepository = jpaTransactionIdempotencyRepository;
    }

    @Override
    public Optional<TransactionIdempotency> getByTransactionId(UUID transactionId) {
        return jpaTransactionIdempotencyRepository.findById(transactionId);
    }

    @Override
    public void save(TransactionIdempotency transaction) {
        jpaTransactionIdempotencyRepository.save(transaction);

    }
}
