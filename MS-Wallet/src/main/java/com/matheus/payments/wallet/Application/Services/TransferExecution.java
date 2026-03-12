package com.matheus.payments.wallet.Application.Services;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Domain.Exceptions.DomainException;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.FailedToSaveLedgerEntry;
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
     * @throws FailedToSaveLedgerEntry            If ledger entry save fails
     * @throws OptimisticLockingFailureException If concurrent update conflict occurs (will be retried)
     */
    @Retry(name = "databaseRetry", fallbackMethod = "handleErrorToExecuteTransfer")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transferExecutionWithRetry(PixTransfer pixTransfer) throws DomainException, FailedToSaveLedgerEntry {
        audit.logBalanceValidation(pixTransfer.getTransactionId().toString());

        Wallet senderWallet = walletService.getWalletById(pixTransfer.getSenderPixKey().getAccountId())
                .orElseThrow(WalletNotFoundException::senderNotFound);

        Wallet receiverWallet = walletService.getWalletById(pixTransfer.getReceiverPixKey().getAccountId())
                .orElseThrow(WalletNotFoundException::receiverNotFound);

        senderWallet.debitAccount(pixTransfer.getAmount());
        receiverWallet.creditAccount(pixTransfer.getAmount());

        registerLedgerEntries(pixTransfer);

        walletService.saveWallet(senderWallet);
        walletService.saveWallet(receiverWallet);
    }

    private void registerLedgerEntries(PixTransfer pixTransfer) throws FailedToSaveLedgerEntry {
        try {
            ledgerService.registryLedgeEntries(pixTransfer);
        } catch (FailedToSaveLedgerEntry e) {
            audit.logFailedGeneric(pixTransfer.getTransactionId().toString(), e.getMessage());
            throw e;
        }
    }

    @Retry(name = "databaseRetry")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void depositExecution(DepositCreated data) {
        Wallet receiverWallet = walletService.getWalletById(data.getReceiverId())
                .orElseThrow(WalletNotFoundException::receiverNotFound);

        receiverWallet.creditAccount(data.getAmount());

        registerDepositLedgerEntries(data);

        walletService.saveWallet(receiverWallet);
    }

    private void registerDepositLedgerEntries(DepositCreated data) throws FailedToSaveLedgerEntry {
        try {
            ledgerService.registryDepositEntryLedge(data);
        } catch (FailedToSaveLedgerEntry e) {
            audit.logFailedGeneric(data.getDepositId().toString(), e.getMessage());
            throw e;
        }
    }

    private void handleErrorToExecuteTransfer(PixTransfer pixTransfer, Throwable throwable) {
        log.error("Error to execute Transfer with transactionId: {}. Cause: {}", pixTransfer.getTransactionId(), throwable.getMessage());

        if(throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        throw new RuntimeException(throwable);
    }
}
