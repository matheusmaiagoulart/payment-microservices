package com.matheus.payments.wallet.Application.Services;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Domain.Exceptions.DomainException;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.FailedToSaveLedgeEntry;
import com.matheus.payments.wallet.Domain.Exceptions.WalletNotFoundException;
import com.matheus.payments.wallet.Domain.Models.Wallet;
import com.matheus.payments.wallet.Infra.Kafka.Listeners.DepositCreated.DepositCreated;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
public class TransferExecution {

    private final LedgerService ledgerService;
    private final WalletService walletService;
    private final WalletServiceAudit audit;

    public TransferExecution(LedgerService ledgerService, WalletService walletService, WalletServiceAudit audit) {
        this.audit = audit;
        this.ledgerService = ledgerService;
        this.walletService = walletService;
    }

    /**
     * This method execute the transfer between wallets, with retry mechanism for Optimistic Locking exceptions.
     *
     * @param pixTransfer Data transfer context
     * @throws DomainException                   If business validation fails (insufficient balance, wallet not found, etc.)
     * @throws FailedToSaveLedgeEntry            If ledger entry save fails
     * @throws OptimisticLockingFailureException If concurrent update conflict occurs (will be retried)
     */
    @Retry(name = "databaseRetry", fallbackMethod = "handleErrorToExecuteTransfer")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transferExecutionWithRetry(PixTransfer pixTransfer) throws DomainException, FailedToSaveLedgeEntry {
        audit.logBalanceValidation(pixTransfer.getTransactionId().toString());

        Wallet senderWallet = walletService.getWalletById(pixTransfer.getSenderPixKey().getAccountId())
                .orElseThrow(WalletNotFoundException::senderNotFound);

        Wallet receiverWallet = walletService.getWalletById(pixTransfer.getReceiverPixKey().getAccountId())
                .orElseThrow(WalletNotFoundException::receiverNotFound);

        senderWallet.debitAccount(pixTransfer.getAmount());
        receiverWallet.creditAccount(pixTransfer.getAmount());

        registryLedgeEntries(pixTransfer);

        walletService.saveWallet(senderWallet);
        walletService.saveWallet(receiverWallet);
    }

    private void registryLedgeEntries(PixTransfer pixTransfer) throws FailedToSaveLedgeEntry {
        try {
            ledgerService.registryLedgeEntries(pixTransfer);
        } catch (FailedToSaveLedgeEntry e) {
            audit.logFailedGeneric(pixTransfer.getTransactionId().toString(), e.getMessage());
            throw e;
        }
    }
}
