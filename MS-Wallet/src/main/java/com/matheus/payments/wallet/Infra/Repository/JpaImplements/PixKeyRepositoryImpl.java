package com.matheus.payments.wallet.Infra.Repository.JpaImplements;

import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Domain.Repositories.PixKeyRepository;
import com.matheus.payments.wallet.Infra.Repository.JpaInterfaces.JpaPixKeyRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PixKeyRepositoryImpl implements PixKeyRepository {

    private final JpaPixKeyRepository jpaPixKeyRepository;

    public PixKeyRepositoryImpl(JpaPixKeyRepository jpaPixKeyRepository) {
        this.jpaPixKeyRepository = jpaPixKeyRepository;
    }

    @Override
    public Optional<PixKey> findAccountIdByKey(String keyValue) {
        return jpaPixKeyRepository.findAccountIdByKey(keyValue);
    }

    @Override
    public PixKey saveAndFlush(PixKey pixKey) {
        return jpaPixKeyRepository.saveAndFlush(pixKey);
    }
}


