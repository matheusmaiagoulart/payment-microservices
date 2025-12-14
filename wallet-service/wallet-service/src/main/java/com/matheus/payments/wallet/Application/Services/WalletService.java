package com.matheus.payments.wallet.Application.Services;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Application.DTOs.Request.CreateWalletRequest;
import com.matheus.payments.wallet.Application.DTOs.Response.InstantPaymentResponse;
import com.matheus.payments.wallet.Domain.Exceptions.*;
import com.matheus.payments.wallet.Domain.TransactionsProcessed;
import com.matheus.payments.wallet.Domain.Wallet;
import com.matheus.payments.wallet.Domain.PixKey;
import com.matheus.payments.wallet.Domain.WalletLedger;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.*;
import com.matheus.payments.wallet.Infra.Repository.TransactionProcessedRepository;
import com.matheus.payments.wallet.Infra.Repository.PixKeyRepository;
import com.matheus.payments.wallet.Infra.Repository.WalletLedgeRepository;
import com.matheus.payments.wallet.Infra.Repository.WalletRepository;
import jakarta.persistence.PersistenceException;
import org.shared.DTOs.TransactionDTO;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletServiceAudit audit;
    private final WalletRepository walletRepository;
    private final PixKeyRepository pixKeyRepository;
    private final WalletLedgeRepository walletLedgeRepository;
    private final TransactionProcessedRepository transactionsProcessed;

    public WalletService(WalletRepository walletRepository, PixKeyRepository pixKeyRepository, WalletServiceAudit audit, TransactionProcessedRepository transactionsProcessed, WalletLedgeRepository walletLedgeRepository) {
        this.audit = audit;
        this.walletRepository = walletRepository;
        this.pixKeyRepository = pixKeyRepository;
        this.walletLedgeRepository = walletLedgeRepository;
        this.transactionsProcessed = transactionsProcessed;
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

    @Transactional
    public InstantPaymentResponse transferProcess(TransactionDTO request) {

        audit.logStartingTransferProcess(request.getTransactionId()); // LOG
        PixTransfer pixTransfer = createPixTransfer(request); // Get all necessary data for the transfer
        try {
            checkTransactionAlreadyProcessed(pixTransfer.getTransactionId()); // Idempotency validation
            transferExecution(pixTransfer);
            registryLedgeEntries(pixTransfer);

            return successTransfer(pixTransfer);
        }
        catch (TransactionAlreadyProcessed e) {
            return idempotencyError(pixTransfer, e);
        }
        catch (DomainException e) {
            return failedTransfer(pixTransfer, e);
        }
    }

    public Optional<Wallet> getWalletById(UUID walletId) {
        return walletRepository.findByAccountIdAndIsActiveTrue(walletId);
    }

    public Optional<WalletKeys> getWalletIdByKey(String keyValue) {
        return walletKeysRepository.findAccountIdByKey(keyValue);
    }

    public int balanceValidation(BigDecimal senderBalance, BigDecimal amount) {
        // Sufficient balance validation (-1 (invalid), 0 (equal), 1 (sufficient))
        return Integer.compare(senderBalance.compareTo(amount), 0);
    }

    public void sameUserValidation(UUID senderWalletId, UUID receiverWalletId) {
        // Same user validation
        if (senderWalletId.equals(receiverWalletId)) {
            throw new SameUserException("Sender and Receiver cannot be the same");
        }
    }

    /**
     * Save transactionId to processed transactions for idempotency control.
     *
     * @param transactionId
     * @throws TransactionAlreadyProcessed If transaction was already processed
     */
    private void saveProcessedTransaction(UUID transactionId) {
        try {
            transactionsProcessed.save(new TransactionsProcessed(transactionId));
        } catch (DataIntegrityViolationException e) {
            throw new TransactionAlreadyProcessed();
        }
    }

    /**
     * This method register ledger entries for debit and credit operations, to audit transactions and wallet operations.
     *
     * @param pixTransfer Data transfer context
     * @throws FailedToSaveLedgeEntry
     */
    private void registryLedgeEntries(PixTransfer pixTransfer) throws FailedToSaveLedgeEntry {
        UUID transactionId = pixTransfer.getTransactionId();
        UUID senderWalletId = pixTransfer.getSenderPixKey().getAccountId();
        UUID receiverWalletId = pixTransfer.getReceiverPixKey().getAccountId();

        WalletLedger entryDebit = new WalletLedger()
                .createDebitEntry(transactionId.toString(), senderWalletId, receiverWalletId, pixTransfer.getAmount());

        WalletLedger entryCredit = new WalletLedger()
                .createCreditEntry(transactionId.toString(), receiverWalletId, senderWalletId, pixTransfer.getAmount());
        try {
            walletLedgeRepository.save(entryDebit);
            walletLedgeRepository.save(entryCredit);
        } catch (DataIntegrityViolationException e) {
            audit.logFailedGeneric(transactionId.toString(), e.getMessage());
            throw new FailedToSaveLedgeEntry(e.getMessage());
        }
    }

    /**
     * This method checks if the transaction was already processed, ensuring idempotency.
     *
     * @param transactionId
     * @throws TransactionAlreadyProcessed
     */
    private void checkTransactionAlreadyProcessed(UUID transactionId) {
        var result = transactionsProcessed.existsById(transactionId);
        if (result) {
            throw new TransactionAlreadyProcessed();
        }
    }

    /**
     * This method populate the PixTransfer, who holds all necessary information for the transfer process.
     *
     * @param request All data from the transfer request
     */
    private PixTransfer createPixTransfer(TransactionDTO request) {
        PixKey accountIdSender = getWalletIdByKey(request.getSenderKey()).orElseThrow(() -> new WalletNotFoundException("Sender"));
        PixKey accountIdReceiver = getWalletIdByKey(request.getReceiverKey()).orElseThrow(() -> new WalletNotFoundException("Receiver"));

        Wallet senderWallet = getWalletById(accountIdSender.getAccountId()).orElseThrow(() -> new WalletNotFoundException("Sender"));
        Wallet receiverWallet = getWalletById(accountIdReceiver.getAccountId()).orElseThrow(() -> new WalletNotFoundException("Receiver"));

        return new PixTransfer(request.getTransactionId(), senderWallet, receiverWallet, accountIdSender, accountIdReceiver, request.getAmount());
    }

    private void transferExecution(PixTransfer pixTransfer) {
        sameUserValidation(pixTransfer.getSenderPixKey().getAccountId(), pixTransfer.getReceiverPixKey().getAccountId());

        audit.logBalanceValidation(pixTransfer.getTransactionId().toString()); // LOG

        // Sufficient balance validation is done inside debitAccount method
        pixTransfer.getSenderWallet().debitAccount(pixTransfer.getAmount());
        pixTransfer.getReceiverWallet().creditAccount(pixTransfer.getAmount());

        walletRepository.save(pixTransfer.getSenderWallet());
        walletRepository.save(pixTransfer.getReceiverWallet());

        saveProcessedTransaction(pixTransfer.getTransactionId()); // Idempotency
    }
}
