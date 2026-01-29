package com.matheus.payments.wallet.Application.Services;

import com.matheus.payments.wallet.Domain.Models.Wallet;
import com.matheus.payments.wallet.Infra.Repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Optional<Wallet> getWalletById(UUID walletId) {
        return walletRepository.findByAccountIdAndIsActiveTrue(walletId);
    }

    public Wallet saveWallet(Wallet wallet) {
        return walletRepository.saveAndFlush(wallet);
    }

    public boolean existsBySocialId(String socialId) {
        return walletRepository.existsBySocialId(socialId);
    }

}
