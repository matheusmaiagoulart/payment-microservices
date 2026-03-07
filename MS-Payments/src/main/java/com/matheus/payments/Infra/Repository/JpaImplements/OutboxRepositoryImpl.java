package com.matheus.payments.Infra.Repository.JpaImplements;

import com.matheus.payments.Domain.Models.TransactionOutbox;
import com.matheus.payments.Domain.Repositories.OutboxRepository;
import com.matheus.payments.Infra.Repository.JpaInterfaces.JpaOutboxRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OutboxRepositoryImpl implements OutboxRepository {

    private final JpaOutboxRepository outboxRepository;
    
    public OutboxRepositoryImpl(JpaOutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Override
    public void save(TransactionOutbox outbox) {
        outboxRepository.save(outbox);
    }

    @Override
    public Optional<TransactionOutbox> findByTransactionId(String transactionId) {
        return outboxRepository.findByTransactionId(transactionId);
    }

    @Override
    public List<TransactionOutbox> findBySentFalseOrderByCreatedAtAsc() {
        return outboxRepository.findBySentFalseOrderByCreatedAtAsc();
    }
}
