package com.matheus.payments.wallet.Application;

import com.matheus.payments.wallet.Application.DTOs.Request.CreateWalletRequest;
import com.matheus.payments.wallet.Application.DTOs.Request.TransactionDTO;
import com.matheus.payments.wallet.Application.DTOs.Response.InstantPaymentResponse;
import com.matheus.payments.wallet.Domain.Wallet.Wallet;
import com.matheus.payments.wallet.Domain.Wallet.WalletKeys;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.InsuficientBalanceException;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.KeyValueAlreadyExists;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.SameUserException;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.WalletNotFoundException;
import com.matheus.payments.wallet.Infra.Repository.WalletKeysRepository;
import com.matheus.payments.wallet.Infra.Repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletKeysRepository walletKeysRepository;

    public WalletService(WalletRepository walletRepository, WalletKeysRepository walletKeysRepository) {
        this.walletKeysRepository = walletKeysRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public boolean createWallet(CreateWalletRequest request) {

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
    }

    @Transactional
    public InstantPaymentResponse handlePaymentProcessor(TransactionDTO request) {

        WalletKeys accountIdSender;
        WalletKeys accountIdReceiver;
        Wallet senderWallet = null;
        Wallet receiverWallet = null;

        try {
            accountIdSender = walletKeysRepository.findwalletIdBykey(request.getSenderKey())
                    .orElseThrow(() -> new WalletNotFoundException("Sender wallet key not found"));

            accountIdReceiver = walletKeysRepository.findwalletIdBykey(request.getReceiverKey())
                    .orElseThrow(() -> new WalletNotFoundException("Receiver wallet key not found"));

            // Same user validation
            if (accountIdSender.getWalletId().equals(accountIdReceiver.getWalletId())) {
                throw new SameUserException("Sender and Receiver cannot be the same");
            }

            senderWallet = walletRepository.findByAccountIdAndIsActiveTrue(accountIdSender.getWalletId())
                    .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found or inactive"));

            receiverWallet = walletRepository.findByAccountIdAndIsActiveTrue(accountIdReceiver.getWalletId())
                    .orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found or inactive"));

            // Sufficient balance validation (-1 (invalid), 0 (equal), 1 (sufficient))
            if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsuficientBalanceException("Insufficient funds in sender's wallet");
            }

            BigDecimal amount = request.getAmount();

            senderWallet.debitAccount(amount);
            receiverWallet.creditAccount(amount);

            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);

        } catch (Exception e) {
            return switch (e) {
                case WalletNotFoundException walletNotFoundException ->
                        new InstantPaymentResponse(false, null, null, e.getMessage());
                case SameUserException sameUserException ->
                        new InstantPaymentResponse(false, null, null, e.getMessage());
                case InsuficientBalanceException insuficientBalanceException ->
                        new InstantPaymentResponse(false, senderWallet.getAccountId(), null, e.getMessage());
                default ->
                        new InstantPaymentResponse(false, senderWallet.getAccountId(), receiverWallet.getAccountId(), "Transaction failed during processing: " + e.getMessage());
            };
        }

        return new InstantPaymentResponse(true, senderWallet.getAccountId(), receiverWallet.getAccountId());

    }

}
