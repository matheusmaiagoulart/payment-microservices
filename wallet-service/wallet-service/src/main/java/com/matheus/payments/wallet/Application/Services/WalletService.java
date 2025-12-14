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
        catch (WalletNotFoundException | SameUserException | InsufficientBalanceException | TransactionAlreadyProcessed | DataAccessException e) {

            audit.logTransferError(request.getTransactionId(), e.getMessage()); // LOG

            // If transaction already processed, consider it a success = true because the transfer was already made
            boolean success = e instanceof TransactionAlreadyProcessed; // Can be true or false, depending on the exception (only true if TransactionAlreadyProcessed)

            UUID senderId = senderWallet != null ? senderWallet.getAccountId() : null;
            UUID receiverId = receiverWallet != null ? receiverWallet.getAccountId() : null;

            return new InstantPaymentResponse(success, senderId, receiverId, e.getMessage());
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

    public void saveTransactionProcessed(TransactionsProcessed transaction) {
        try {
            transactionsProcessed.save(transaction);
        } catch (DataIntegrityViolationException e) {
            throw new TransactionAlreadyProcessed();
        }
    }

}
