package com.matheus.payments.Infra.Repository.JpaImplements;

import com.matheus.payments.Domain.Exceptions.DepositNotFound;
import com.matheus.payments.Domain.Models.Deposit;
import com.matheus.payments.Domain.Repositories.DepositRepository;
import com.matheus.payments.Infra.Repository.JpaInterfaces.JpaDepositRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class DepositRepositoryImpl implements DepositRepository {

    private final JpaDepositRepository jpaDepositRepository;

    public DepositRepositoryImpl(JpaDepositRepository jpaDepositRepository) {
        this.jpaDepositRepository = jpaDepositRepository;
    }

    @Override
    public void saveDeposit(Deposit deposit) {
        jpaDepositRepository.save(deposit);
    }

    @Override
    public Optional<Deposit> getDepositById(UUID depositId) {
        return jpaDepositRepository.findById(depositId);
    }
}
