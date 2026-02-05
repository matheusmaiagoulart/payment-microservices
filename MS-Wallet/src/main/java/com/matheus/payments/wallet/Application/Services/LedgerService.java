package com.matheus.payments.wallet.Application.Services;

import com.matheus.payments.wallet.Application.Audit.LedgerAudit;
import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Domain.Models.WalletLedger;
import com.matheus.payments.wallet.Domain.Exceptions.FailedToSaveLedgeEntry;
import com.matheus.payments.wallet.Infra.Repository.WalletLedgeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerAudit audit;
    private final WalletLedgeRepository walletLedgeRepository;

    public LedgerService(LedgerAudit audit, WalletLedgeRepository walletLedgeRepository) {
        this.audit = audit;
        this.walletLedgeRepository = walletLedgeRepository;
    }

    /**
     * This method register ledger entries for debit and credit operations, to audit transactions and wallet operations.
     *
     * @param pixTransfer Data transfer context
     * @throws FailedToSaveLedgeEntry
     */
    public void registryLedgeEntries(PixTransfer pixTransfer) throws FailedToSaveLedgeEntry {
        String transactionId = pixTransfer.getTransactionId().toString();
        UUID senderWalletId = pixTransfer.getSenderPixKey().getAccountId();
        UUID receiverWalletId = pixTransfer.getReceiverPixKey().getAccountId();
        BigDecimal amount = pixTransfer.getAmount();

        WalletLedger entryDebit = new WalletLedger().createDebitEntry(transactionId, senderWalletId, receiverWalletId, amount);

        WalletLedger entryCredit = new WalletLedger().createCreditEntry(transactionId, receiverWalletId, senderWalletId, amount);
        try {
            walletLedgeRepository.saveAndFlush(entryDebit);
            walletLedgeRepository.saveAndFlush(entryCredit);
        } catch (DataIntegrityViolationException e) {
            audit.logFailedCreateLedgerEntries(transactionId, pixTransfer.getSenderPixKey().getKeyValue());
            throw new FailedToSaveLedgeEntry("Failed to save ledger entries for transactionId: " + transactionId);
        }
    }
}
