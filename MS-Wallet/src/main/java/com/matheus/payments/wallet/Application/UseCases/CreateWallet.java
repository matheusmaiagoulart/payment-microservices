package com.matheus.payments.wallet.Application.UseCases;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Infra.Kafka.Listeners.UserCreated.UserCreatedEvent;
import com.matheus.payments.wallet.Domain.Exceptions.PixKeyAlreadyRegisteredException;
import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Domain.Models.Wallet;
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
    public boolean createWallet(UserCreatedEvent request) throws PersistenceException, PixKeyAlreadyRegisteredException {
        audit.logCreatingWallet(request.getKeyValue());

        if (pixKeyRepository.existsWalletKeysByKeyValue(request.getKeyValue())) {
            audit.logFailedCreateWallet(request.getKeyValue());
            throw new PixKeyAlreadyRegisteredException(request.getKeyValue());
        }

        try {
            Wallet walletCreated = persistWallet(request);
            createPixKey(request, walletCreated);
            return true;
        } catch (PersistenceException e) {
            audit.logFailedGeneric(request.getKeyValue(), e.getMessage());
            throw new PersistenceException("An error occurred while creating the wallet: " + e.getMessage());
        }
    }

    private Wallet persistWallet(UserCreatedEvent request) throws PersistenceException {
        Wallet wallet = new Wallet(request.getAccountId(), request.getAccountType());
        walletRepository.save(wallet);
        return wallet;
    }

    private void createPixKey(UserCreatedEvent request, Wallet wallet) throws PersistenceException {
        PixKey walletKeys = new PixKey(request.getKeyValue(), request.getKeyType(), wallet.getAccountId());
        pixKeyRepository.save(walletKeys);
    }
}
