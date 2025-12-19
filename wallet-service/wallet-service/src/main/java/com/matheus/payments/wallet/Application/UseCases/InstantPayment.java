package com.matheus.payments.wallet.Application.UseCases;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Application.DTOs.Context.WalletTransfer;
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
    private final WalletRepository walletRepository;
    private final PixKeyRepository pixKeyRepository;
    private final WalletLedgeRepository walletLedgeRepository;
    private final TransactionProcessedRepository transactionsProcessedRepository;

    public InstantPayment(WalletRepository walletRepository, PixKeyRepository pixKeyRepository, WalletServiceAudit audit, TransactionProcessedRepository transactionsProcessedRepository, WalletLedgeRepository walletLedgeRepository) {
        this.audit = audit;
        this.walletRepository = walletRepository;
        this.pixKeyRepository = pixKeyRepository;
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
            registryLedgeEntries(pixTransfer);

            return successTransfer(pixTransfer);
        }
        catch (OptimisticLockingFailureException e) {
        audit.logFailedGeneric(pixTransfer.getTransactionId().toString(), "Max retry attempts reached for transaction due to concurrent updates.");
            return failedTransfer(pixTransfer, new ConcurrentTransactionException());
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

    public Optional<PixKey> getWalletIdByKey(String keyValue) {
        return pixKeyRepository.findAccountIdByKey(keyValue);
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
            transactionsProcessedRepository.save(new TransactionsProcessed(transactionId));
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
        PixKey accountIdSender = getWalletIdByKey(request.getSenderKey()).orElseThrow(() -> new WalletNotFoundException("Sender"));
        PixKey accountIdReceiver = getWalletIdByKey(request.getReceiverKey()).orElseThrow(() -> new WalletNotFoundException("Receiver"));

        Wallet senderWallet = getWalletById(accountIdSender.getAccountId()).orElseThrow(() -> new WalletNotFoundException("Sender"));
        Wallet receiverWallet = getWalletById(accountIdReceiver.getAccountId()).orElseThrow(() -> new WalletNotFoundException("Receiver"));

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
                pixTransfer.getReceiverPixKey().getAccountId()
        );

        audit.logBalanceValidation(pixTransfer.getTransactionId().toString());

        Wallet senderWallet = getWalletById(pixTransfer.getSenderPixKey().getAccountId())
                .orElseThrow(() -> new WalletNotFoundException("Sender"));

        Wallet receiverWallet = getWalletById(pixTransfer.getReceiverPixKey().getAccountId())
                .orElseThrow(() -> new WalletNotFoundException("Receiver"));

        WalletTransfer walletTransfer =
                new WalletTransfer(senderWallet, receiverWallet, pixTransfer.getAmount()); // Created again to ensure latest data from DB

        walletTransfer.getSenderWallet().debitAccount(walletTransfer.getAmount());
        walletTransfer.getReceiverWallet().creditAccount(walletTransfer.getAmount());

        walletRepository.save(walletTransfer.getSenderWallet());
        walletRepository.save(walletTransfer.getReceiverWallet());

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
