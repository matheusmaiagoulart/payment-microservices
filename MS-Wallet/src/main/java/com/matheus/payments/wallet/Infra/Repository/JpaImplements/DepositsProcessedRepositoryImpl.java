package com.matheus.payments.wallet.Infra.Repository.JpaImplements;

import com.matheus.payments.wallet.Domain.Models.DepositsProcessed;
import com.matheus.payments.wallet.Domain.Repositories.DepositsProcessedRepository;
import com.matheus.payments.wallet.Infra.Repository.JpaInterfaces.JpaDepositsProcessedRepository;
import org.springframework.stereotype.Repository;

@Repository
public class DepositsProcessedRepositoryImpl implements DepositsProcessedRepository {

    private final JpaDepositsProcessedRepository jpaDepositsProcessedRepository;

    public DepositsProcessedRepositoryImpl(JpaDepositsProcessedRepository jpaDepositsProcessedRepository) {
        this.jpaDepositsProcessedRepository = jpaDepositsProcessedRepository;
    }

    @Override
    public DepositsProcessed saveAndFlush(DepositsProcessed depositsProcessed) {
        return jpaDepositsProcessedRepository.saveAndFlush(depositsProcessed);
    }
}

