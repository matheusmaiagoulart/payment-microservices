package com.matheus.payments.wallet.Application.UseCases;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.DTOs.Request.CreateWalletRequest;
import com.matheus.payments.wallet.Application.Interfaces.ICreateWallet;
import com.matheus.payments.wallet.Domain.Exceptions.PixKeyAlreadyRegisteredException;
import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Domain.Models.Wallet;
import com.matheus.payments.wallet.Infra.Repository.PixKeyRepository;
import com.matheus.payments.wallet.Infra.Repository.WalletRepository;
import jakarta.persistence.PersistenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateWallet implements ICreateWallet {

    private final WalletServiceAudit audit;
    private final WalletRepository walletRepository;
    private final PixKeyRepository pixKeyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public CreateWallet(WalletRepository walletRepository, PixKeyRepository pixKeyRepository, WalletServiceAudit audit) {
        this.audit = audit;
        this.walletRepository = walletRepository;
        this.pixKeyRepository = pixKeyRepository;
    }

    @Transactional
    public boolean createWallet(CreateWalletRequest request) {
        try {
            audit.logCreatingWallet(request.getKeyValue()); // LOG

            boolean keyExists = pixKeyRepository.existsWalletKeysByKeyValue(request.getKeyValue());

            if (keyExists) {
                audit.logFailedCreateWallet(request.getKeyValue());
                throw new PixKeyAlreadyRegisteredException(request.getKeyValue());
            }

            Wallet wallet = new Wallet(request.getAccountId(), request.getAccountType());
            walletRepository.save(wallet);

            PixKey walletKeys = new PixKey(request.getKeyValue(), request.getKeyType(), wallet.getAccountId());
            pixKeyRepository.save(walletKeys);
            return true;
        } catch (PersistenceException e) {
            audit.logFailedGeneric("An error occurred while creating the wallet: " + e.getMessage(), request.getKeyValue());
            throw new PersistenceException("An error occurred while creating the wallet: " + e.getMessage());
        }
    }
}
