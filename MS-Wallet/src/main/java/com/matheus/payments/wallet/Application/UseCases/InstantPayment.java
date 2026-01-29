package com.matheus.payments.wallet.Application.UseCases;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Application.DTOs.Context.WalletTransfer;
import com.matheus.payments.wallet.Application.DTOs.Response.InstantPaymentResponse;
import com.matheus.payments.wallet.Application.Services.PixKeyService;
import com.matheus.payments.wallet.Application.Services.WalletService;
import com.matheus.payments.wallet.Domain.Exceptions.*;
import com.matheus.payments.wallet.Domain.Models.TransactionsProcessed;
import com.matheus.payments.wallet.Domain.Models.Wallet;
import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Domain.Models.WalletLedger;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.*;
import com.matheus.payments.wallet.Infra.Repository.TransactionProcessedRepository;
import com.matheus.payments.wallet.Infra.Repository.WalletLedgeRepository;
import com.matheus.payments.wallet.Infra.Repository.WalletRepository;
import org.shared.DTOs.TransactionDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class InstantPayment {

    private final WalletServiceAudit audit;
    private final WalletService walletService;
    private final PixKeyService pixKeyService;
    private final WalletLedgeRepository walletLedgeRepository;
    private final TransactionProcessedRepository transactionsProcessedRepository;

    public InstantPayment(PixKeyService pixKeyService, WalletService walletService, WalletServiceAudit audit, TransactionProcessedRepository transactionsProcessedRepository, WalletLedgeRepository walletLedgeRepository) {
        this.audit = audit;
        this.walletService = walletService;
        this.pixKeyService = pixKeyService;
        this.walletLedgeRepository = walletLedgeRepository;
        this.transactionsProcessedRepository = transactionsProcessedRepository;
    }


    @Transactional
    public InstantPaymentResponse transferProcess(TransactionDTO request) {

        audit.logStartingTransferProcess(request.getTransactionId()); // LOG
        PixTransfer pixTransfer = createPixTransfer(request); // Get all necessary data for the transfer
        try {
            checkTransactionAlreadyProcessed(UUID.fromString(request.getTransactionId())); // Idempotency validation
            transferExecutionWithRetry(pixTransfer);
            return successTransfer(pixTransfer);
        } catch (OptimisticLockingFailureException e) {
            audit.logFailedGeneric(pixTransfer.getTransactionId().toString(), "Max retry attempts reached for transaction due to concurrent updates.");
            return failedTransfer(pixTransfer, new ConcurrentTransactionException());
        } catch (TransactionAlreadyProcessed e) {
            return idempotencyError(pixTransfer, e);
        } catch (DomainException e) {
            return failedTransfer(pixTransfer, e);
        }
    }

    private void sameUserValidation(UUID senderWalletId, UUID receiverWalletId) {
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
            transactionsProcessedRepository.saveAndFlush(new TransactionsProcessed(transactionId));
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
            walletLedgeRepository.saveAndFlush(entryDebit);
            walletLedgeRepository.saveAndFlush(entryCredit);
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
        var result = transactionsProcessedRepository.existsById(transactionId);
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
        PixKey accountIdSender = pixKeyService.getWalletIdByKey(request.getSenderKey()).orElseThrow(() -> new WalletNotFoundException("Sender"));
        PixKey accountIdReceiver = pixKeyService.getWalletIdByKey(request.getReceiverKey()).orElseThrow(() -> new WalletNotFoundException("Receiver"));

        Wallet senderWallet = walletService.getWalletById(accountIdSender.getAccountId()).orElseThrow(() -> new WalletNotFoundException("Sender"));
        Wallet receiverWallet = walletService.getWalletById(accountIdReceiver.getAccountId()).orElseThrow(() -> new WalletNotFoundException("Receiver"));

        return new PixTransfer(request.getTransactionId(), senderWallet, receiverWallet, accountIdSender, accountIdReceiver, request.getAmount());
    }

    /**
     * This method execute the transfer between wallets, with retry mechanism for Optimistic Locking exceptions.
     *
     * @param pixTransfer Data transfer context
     */
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional
    public void transferExecutionWithRetry(PixTransfer pixTransfer) {

        sameUserValidation(
                pixTransfer.getSenderPixKey().getAccountId(),
                pixTransfer.getReceiverPixKey().getAccountId());

        audit.logBalanceValidation(pixTransfer.getTransactionId().toString());

        Wallet senderWallet = walletService.getWalletById(pixTransfer.getSenderPixKey().getAccountId())
                .orElseThrow(() -> new WalletNotFoundException("Sender"));

        Wallet receiverWallet = walletService.getWalletById(pixTransfer.getReceiverPixKey().getAccountId())
                .orElseThrow(() -> new WalletNotFoundException("Receiver"));

        WalletTransfer walletTransfer =
                new WalletTransfer(senderWallet, receiverWallet, pixTransfer.getAmount()); // Created again to ensure latest data from DB

        walletTransfer.senderWallet().debitAccount(walletTransfer.amount());
        walletTransfer.receiverWallet().creditAccount(walletTransfer.amount());

        walletService.saveWallet(walletTransfer.senderWallet());
        walletService.saveWallet(walletTransfer.receiverWallet());

        registryLedgeEntries(pixTransfer);

        saveProcessedTransaction(pixTransfer.getTransactionId());
    }

    /**
     * Methods to build different response types, according to transfer process result
     *
     * @param pixTransfer
     */
    private InstantPaymentResponse successTransfer(PixTransfer pixTransfer) {
        audit.logTransferSuccess(pixTransfer.getTransactionId().toString()); // LOG
        return new InstantPaymentResponse(true, false, pixTransfer.getSenderWallet().getAccountId(), pixTransfer.getReceiverWallet().getAccountId());
    }

    private InstantPaymentResponse idempotencyError(PixTransfer pixTransfer, TransactionAlreadyProcessed e) {
        audit.logTransferError(pixTransfer.getTransactionId().toString(), e.getMessage()); // LOG
        return new InstantPaymentResponse(true, true, pixTransfer.getSenderWallet().getAccountId(), pixTransfer.getReceiverWallet().getAccountId(), e.getMessage());
    }

    private InstantPaymentResponse failedTransfer(PixTransfer pixTransfer, Exception e) {
        audit.logTransferError(pixTransfer.getTransactionId().toString(), e.getMessage()); // LOG

        UUID senderId = pixTransfer.getSenderWallet() != null ? pixTransfer.getSenderWallet().getAccountId() : null;
        UUID receiverId = pixTransfer.getReceiverWallet() != null ? pixTransfer.getReceiverWallet().getAccountId() : null;
        return new InstantPaymentResponse(false, false, senderId, receiverId, e.getMessage());
    }
}
