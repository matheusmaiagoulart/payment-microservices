package com.matheus.payments.wallet.Application.UseCases;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.Events.CreateWallet.WalletCreatedEvent;
import com.matheus.payments.wallet.Application.Events.CreateWallet.WalletCreationFailed;
import com.matheus.payments.wallet.Application.Services.PixKeyService;
import com.matheus.payments.wallet.Application.Services.WalletService;
import com.matheus.payments.wallet.Domain.Exceptions.SocialIdAlreadyExistsException;
import com.matheus.payments.wallet.Infra.Kafka.Listeners.UserCreated.UserCreatedEvent;
import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Domain.Models.Wallet;
import jakarta.persistence.PersistenceException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CreateWallet {

    private final WalletServiceAudit audit;
    private final WalletService walletService;
    private final PixKeyService pixKeyService;
    private final ApplicationEventPublisher internalEventPublisher;

    public CreateWallet(WalletService walletService, PixKeyService pixKeyService, WalletServiceAudit audit, ApplicationEventPublisher internalEventPublisher) {
        this.audit = audit;
        this.walletService = walletService;
        this.pixKeyService = pixKeyService;
        this.internalEventPublisher = internalEventPublisher;
    }

    @Transactional
    public boolean createWallet(UserCreatedEvent request) throws PersistenceException, SocialIdAlreadyExistsException {
        audit.logCreatingWallet(request.getKeyValue());
        try {
            if (walletService.existsBySocialId(request.getKeyValue())) {
                audit.logFailedCreateWallet(request.getKeyValue());
                throw new SocialIdAlreadyExistsException(request.getKeyValue());
            }

            Wallet walletCreated = persistWallet(request);
            createPixKey(request, walletCreated);

            publishSuccess(request);
            return true;
        }
        catch (SocialIdAlreadyExistsException e) {
            audit.logFailedToProcessMessage(request.getKeyValue(), e.getMessage());
            return false;

        } catch (PersistenceException | DataAccessException e) {
            handleFailure(request, "DATABASE_ERROR", e);
            return false;

        } catch (Exception e) {
            handleFailure(request, "UNKNOWN_ERROR", e);
            return false;
        }
    }

    private Wallet persistWallet(UserCreatedEvent request) throws PersistenceException {
        Wallet wallet = new Wallet(request.getAccountId(), request.getAccountType(), request.getKeyValue());
        return walletService.saveWallet(wallet);
    }

    private PixKey createPixKey(UserCreatedEvent request, Wallet wallet) throws PersistenceException {
        PixKey walletKeys = new PixKey(request.getKeyValue(), request.getKeyType(), wallet.getAccountId());
        return pixKeyService.savePixKey(walletKeys);
    }

    private void publishSuccess(UserCreatedEvent request) {
        internalEventPublisher.publishEvent(
                new WalletCreatedEvent(request.getAccountId(), request.getKeyValue())
        );
    }

    private void handleFailure(UserCreatedEvent request, String errorMessage, Exception e) {
        String keyValue = (request != null) ? request.getKeyValue() : null;
        audit.logFailedToProcessMessage(keyValue, errorMessage + e.getMessage());

        if (request != null) {
            internalEventPublisher.publishEvent(
                    new WalletCreationFailed(request.getAccountId(), request.getKeyValue(), errorMessage)
            );
        }
    }
}
