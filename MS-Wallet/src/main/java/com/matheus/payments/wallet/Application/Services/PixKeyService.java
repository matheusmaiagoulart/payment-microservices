package com.matheus.payments.wallet.Application.Services;

import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Domain.Models.Wallet;
import com.matheus.payments.wallet.Infra.Repository.PixKeyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PixKeyService {

    private final PixKeyRepository pixKeyRepository;

    public PixKeyService(PixKeyRepository pixKeyRepository) {
        this.pixKeyRepository = pixKeyRepository;
    }

    public Optional<PixKey> getWalletIdByKey(String keyValue) {
        return pixKeyRepository.findAccountIdByKey(keyValue);
    }

    public PixKey savePixKey(PixKey pixKey) {
        return pixKeyRepository.saveAndFlush(pixKey);
    }

}
