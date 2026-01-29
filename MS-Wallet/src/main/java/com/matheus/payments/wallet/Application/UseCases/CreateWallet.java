package com.matheus.payments.wallet.Application.UseCases;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.Services.PixKeyService;
import com.matheus.payments.wallet.Application.Services.WalletService;
import com.matheus.payments.wallet.Domain.Exceptions.SocialIdAlreadyExistsException;
import com.matheus.payments.wallet.Infra.Kafka.Listeners.UserCreated.UserCreatedEvent;
import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Domain.Models.Wallet;
import com.matheus.payments.wallet.Infra.Repository.PixKeyRepository;
import com.matheus.payments.wallet.Infra.Repository.WalletRepository;
import jakarta.persistence.PersistenceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CreateWallet {

    private final WalletServiceAudit audit;
    private final WalletService walletService;
    private final PixKeyService pixKeyService;

    public CreateWallet(WalletService walletService, PixKeyService pixKeyService, WalletServiceAudit audit) {
        this.audit = audit;
        this.walletService = walletService;
        this.pixKeyService = pixKeyService;
    }

    @Transactional
    public boolean createWallet(UserCreatedEvent request) throws PersistenceException, SocialIdAlreadyExistsException {
        audit.logCreatingWallet(request.getKeyValue());

        if (walletService.existsBySocialId(request.getKeyValue())) {
            audit.logFailedCreateWallet(request.getKeyValue());
            throw new SocialIdAlreadyExistsException(request.getKeyValue());
        }

        try {
            Wallet walletCreated = persistWallet(request);
            createPixKey(request, walletCreated);
            return true;
        } catch (DataIntegrityViolationException e) {
            audit.logFailedGeneric(request.getKeyValue(), e.getMessage());
            throw new DataIntegrityViolationException("An error occurred while creating the wallet: " + e.getMessage());
        }
    }

    private Wallet persistWallet(UserCreatedEvent request) throws PersistenceException {
        Wallet wallet = new Wallet(request.getAccountId(), request.getAccountType(), request.getKeyValue());
        return walletService.saveWallet(wallet);
    }

    private void createPixKey(UserCreatedEvent request, Wallet wallet) throws PersistenceException {
        PixKey walletKeys = new PixKey(request.getKeyValue(), request.getKeyType(), wallet.getAccountId());
        pixKeyService.savePixKey(walletKeys);
    }
}
