package com.matheus.payments.wallet.Application.Services;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.DTOs.Request.CreateWalletRequest;
import com.matheus.payments.wallet.Application.DTOs.Response.InstantPaymentResponse;
import com.matheus.payments.wallet.Domain.TransactionsProcessed;
import com.matheus.payments.wallet.Domain.Wallet;
import com.matheus.payments.wallet.Domain.WalletKeys;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.*;
import com.matheus.payments.wallet.Infra.Repository.TransactionProcessedRepository;
import com.matheus.payments.wallet.Infra.Repository.WalletKeysRepository;
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
    private final WalletKeysRepository walletKeysRepository;
    private final TransactionProcessedRepository transactionsProcessed;

    public WalletService(WalletRepository walletRepository, WalletKeysRepository walletKeysRepository, WalletServiceAudit audit, TransactionProcessedRepository transactionsProcessed) {
        this.audit = audit;
        this.walletRepository = walletRepository;
        this.walletKeysRepository = walletKeysRepository;
        this.transactionsProcessed = transactionsProcessed;
    }

    @Transactional
    public boolean createWallet(CreateWalletRequest request) {

        try {
            System.out.println("Creating wallet for user: " + request.keyValue);

            boolean keyExists = walletKeysRepository.existsWalletKeysByKeyValue(request.keyValue);

            if (keyExists) {
                throw new KeyValueAlreadyExists("Key value already exists: " + request.keyValue);
            }

            Wallet wallet = new Wallet(request.accountType);
            walletRepository.save(wallet);

            WalletKeys walletKeys = new WalletKeys(request.keyValue, request.keyType, wallet.getAccountId());
            walletKeysRepository.save(walletKeys);

            return true;
        } catch (PersistenceException e) {
            throw new PersistenceException("An error occurred while creating the wallet: " + e.getMessage());
        }
    }

    @Transactional
    public InstantPaymentResponse transferProcess(TransactionDTO request) {

        audit.logStartingTransferProcess(request.getTransactionId()); // LOG

        WalletKeys accountIdSender = getWalletIdByKey(request.getSenderKey())
                .orElseThrow(() -> new WalletNotFoundException("Sender account not found"));

        WalletKeys accountIdReceiver = getWalletIdByKey(request.getReceiverKey())
                .orElseThrow(() -> new WalletNotFoundException("Receiver account not found"));


        sameUserValidation(accountIdSender.getAccountId(), accountIdReceiver.getAccountId());

        Wallet senderWallet = getWalletById(accountIdSender.getAccountId()).orElseThrow(() ->
                new WalletNotFoundException("Sender wallet not found or inactive"));


        Wallet receiverWallet = getWalletById(accountIdReceiver.getAccountId()).orElseThrow(() ->
                new WalletNotFoundException("Receiver wallet not found or inactive"));

        try {
            audit.logBalanceValidation(request.getTransactionId()); // LOG
            var resultValidationBalance = balanceValidation(senderWallet.getBalance(), request.getAmount());
            if (resultValidationBalance < 0) {
                throw new InsufficientBalanceException("Insufficient funds in sender's wallet");
            }

            BigDecimal amount = request.getAmount();

            senderWallet.debitAccount(amount);
            receiverWallet.creditAccount(amount);

            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);

            saveTransactionProcessed(new TransactionsProcessed(UUID.fromString(request.getTransactionId()))); // Idempotency

            audit.logTransferSuccess(request.getTransactionId()); // LOG

        } catch (Exception e) {

            audit.logTransferError(request.getTransactionId(), e.getMessage()); // LOG
            return switch (e) {
                case WalletNotFoundException walletNotFoundException ->
                        new InstantPaymentResponse(false, null, null, e.getMessage());

                case SameUserException sameUserException ->
                        new InstantPaymentResponse(false, null, null, e.getMessage());

                case InsufficientBalanceException insufficientBalanceException ->
                        new InstantPaymentResponse(false, senderWallet.getAccountId(), receiverWallet.getAccountId(), e.getMessage());

                case DataAccessException databaseException ->
                        new InstantPaymentResponse(false, senderWallet.getAccountId(), receiverWallet.getAccountId(), "Database error occurred: " + e.getMessage());

                case TransactionAlreadyProcessed transactionAlreadyProcessed ->
                        new InstantPaymentResponse(true, senderWallet.getAccountId(), receiverWallet.getAccountId(), e.getMessage());

                default -> new InstantPaymentResponse(
                        false,
                        senderWallet != null ? senderWallet.getAccountId() : null,
                        receiverWallet != null ? receiverWallet.getAccountId() : null,
                        "Transaction failed during processing: " + e.getMessage());
            };
        }
        return new InstantPaymentResponse(true, senderWallet.getAccountId(), receiverWallet.getAccountId());
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
