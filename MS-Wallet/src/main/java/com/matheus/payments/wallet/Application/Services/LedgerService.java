package com.matheus.payments.wallet.Application.Services;

import com.matheus.payments.wallet.Application.Audit.LedgerAudit;
import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Domain.Models.WalletLedger;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.FailedToSaveLedgeEntry;
import com.matheus.payments.wallet.Infra.Kafka.Listeners.DepositCreated.DepositCreated;
import com.matheus.payments.wallet.Domain.Repositories.WalletLedgerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerAudit audit;
    private final WalletLedgerRepository walletLedgerRepository;

    public LedgerService(LedgerAudit audit, WalletLedgerRepository walletLedgerRepository) {
        this.audit = audit;
        this.walletLedgerRepository = walletLedgerRepository;
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
            walletLedgerRepository.saveAndFlush(entryDebit);
            walletLedgerRepository.saveAndFlush(entryCredit);
        } catch (DataIntegrityViolationException e) {
            audit.logFailedCreateLedgerEntries(transactionId, pixTransfer.getSenderPixKey().getKeyValue());
            throw new FailedToSaveLedgeEntry(transactionId);
        }
    }

    public void registryDepositEntryLedge(DepositCreated deposit) {
        String transactionId = deposit.getDepositId().toString();
        UUID receiverWalletId = deposit.getReceiverId();
        BigDecimal amount = deposit.getAmount();

        WalletLedger entryCredit = new WalletLedger().createDepositEntry(transactionId, receiverWalletId, amount);

        try {
            walletLedgerRepository.saveAndFlush(entryCredit);
        } catch (DataIntegrityViolationException e) {
            audit.logFailedCreateLedgerEntries(transactionId, receiverWalletId.toString());
            throw new FailedToSaveLedgeEntry(transactionId);
        }
    }
}
