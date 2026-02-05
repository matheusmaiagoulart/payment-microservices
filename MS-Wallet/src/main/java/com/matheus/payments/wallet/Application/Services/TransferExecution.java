package com.matheus.payments.wallet.Application.Services;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Domain.Exceptions.FailedToSaveLedgeEntry;
import com.matheus.payments.wallet.Domain.Exceptions.WalletNotFoundException;
import com.matheus.payments.wallet.Domain.Models.Wallet;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


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
     */
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transferExecutionWithRetry(PixTransfer pixTransfer) {
        audit.logBalanceValidation(pixTransfer.getTransactionId().toString());

        Wallet senderWallet = walletService.getWalletById(pixTransfer.getSenderPixKey().getAccountId())
                .orElseThrow(() -> new WalletNotFoundException("Sender"));

        Wallet receiverWallet = walletService.getWalletById(pixTransfer.getReceiverPixKey().getAccountId())
                .orElseThrow(() -> new WalletNotFoundException("Receiver"));

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
