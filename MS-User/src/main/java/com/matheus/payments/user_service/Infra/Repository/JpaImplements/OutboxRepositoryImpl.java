package com.matheus.payments.user_service.Infra.Repository.JpaImplements;

import com.matheus.payments.user_service.Domain.Models.Outbox;
import com.matheus.payments.user_service.Domain.Repositories.OutboxRepository;
import com.matheus.payments.user_service.Infra.Repository.JpaInterfaces.JpaOutboxRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OutboxRepositoryImpl implements OutboxRepository {

    private final JpaOutboxRepository jpaOutboxRepository;

    public OutboxRepositoryImpl(JpaOutboxRepository jpaOutboxRepository) {
        this.jpaOutboxRepository = jpaOutboxRepository;
    }

    @Override
    public Outbox save(Outbox outbox) {
        return jpaOutboxRepository.save(outbox);
    }

    @Override
    public List<Outbox> findAllBySentFalse() {
        return jpaOutboxRepository.findAllBySentFalse();
    }
}

