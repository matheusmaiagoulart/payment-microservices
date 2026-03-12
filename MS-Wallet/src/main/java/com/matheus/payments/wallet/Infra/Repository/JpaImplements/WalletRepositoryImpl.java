package com.matheus.payments.wallet.Infra.Repository.JpaImplements;

import com.matheus.payments.wallet.Domain.Models.Wallet;
import com.matheus.payments.wallet.Domain.Repositories.WalletRepository;
import com.matheus.payments.wallet.Infra.Repository.JpaInterfaces.JpaWalletRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class WalletRepositoryImpl implements WalletRepository {

    private final JpaWalletRepository jpaWalletRepository;

    public WalletRepositoryImpl(JpaWalletRepository jpaWalletRepository) {
        this.jpaWalletRepository = jpaWalletRepository;
    }

    @Override
    public Optional<Wallet> findByAccountIdAndIsActiveTrue(UUID userId) {
        return jpaWalletRepository.findByAccountIdAndIsActiveTrue(userId);
    }

    @Override
    public Boolean existsBySocialId(String socialId) {
        return jpaWalletRepository.existsBySocialId(socialId);
    }

    @Override
    public Wallet saveAndFlush(Wallet wallet) {
        return jpaWalletRepository.saveAndFlush(wallet);
    }
}


