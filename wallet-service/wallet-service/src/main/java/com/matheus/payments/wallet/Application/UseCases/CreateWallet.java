package com.matheus.payments.wallet.Application.UseCases;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.DTOs.Request.CreateWalletRequest;
import com.matheus.payments.wallet.Domain.Exceptions.PixKeyAlreadyRegisteredException;
import com.matheus.payments.wallet.Domain.PixKey;
import com.matheus.payments.wallet.Domain.Wallet;
import com.matheus.payments.wallet.Infra.Repository.PixKeyRepository;
import com.matheus.payments.wallet.Infra.Repository.WalletRepository;
import jakarta.persistence.PersistenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateWallet {

    private final WalletServiceAudit audit;
    private final WalletRepository walletRepository;
    private final PixKeyRepository pixKeyRepository;

    public CreateWallet(WalletRepository walletRepository, PixKeyRepository pixKeyRepository, WalletServiceAudit audit) {
        this.audit = audit;
        this.walletRepository = walletRepository;
        this.pixKeyRepository = pixKeyRepository;
    }

    @Transactional
    public boolean createWallet(CreateWalletRequest request) {
        try {
            audit.logCreatingWallet(request.keyValue); // LOG

            boolean keyExists = pixKeyRepository.existsWalletKeysByKeyValue(request.keyValue);

            if (keyExists) {
                audit.logFailedCreateWallet(request.keyValue);
                throw new PixKeyAlreadyRegisteredException(request.keyValue);
            }

            Wallet wallet = new Wallet(request.accountType);
            walletRepository.save(wallet);

            PixKey walletKeys = new PixKey(request.keyValue, request.keyType, wallet.getAccountId());
            pixKeyRepository.save(walletKeys);
            return true;
        } catch (PersistenceException e) {
            audit.logFailedGeneric("An error occurred while creating the wallet: " + e.getMessage(), request.keyValue);
            throw new PersistenceException("An error occurred while creating the wallet: " + e.getMessage());
        }
    }
}
