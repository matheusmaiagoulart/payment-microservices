package com.matheus.payments.wallet.Application;

import com.matheus.payments.wallet.Application.DTOs.Request.TransactionDTO;
import com.matheus.payments.wallet.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.wallet.Domain.Wallet.Wallet;
import com.matheus.payments.wallet.Domain.Wallet.accountType;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.InsuficientBalanceException;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.SameUserException;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.WalletNotFoundException;
import com.matheus.payments.wallet.Infra.Repository.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {

    private WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }


    public HttpStatus createWallet(UUID userId, accountType accountType) {

        System.out.println("Creating wallet for user: " + userId);

        Wallet wallet = new Wallet(userId, accountType);
        walletRepository.save(wallet);
        return HttpStatus.CREATED;
    }

    @Transactional
    public PaymentProcessorResponse handlePaymentProcessor(TransactionDTO request){

        var payloadResponse = new PaymentProcessorResponse(
                request.getTransactionId(),
                true,
                false,
                null
        );

        try {

            // Same user validation
            if (request.getSenderId().equals(request.getReceiverId())) {
                throw new SameUserException("Sender and receiver cannot be the same");
            }

            Wallet senderWallet = walletRepository.findByUserIdAndIsActiveTrue(request.getSenderId())
                    .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found or inactive"));

            // Sufficient balance validation (-1 (invalid), 0 (equal), 1 (sufficient))
            if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsuficientBalanceException("Insufficient funds in sender's wallet");
            }
            Wallet receiverWallet = walletRepository.findByUserIdAndIsActiveTrue(request.getReceiverId())
                    .orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found or inactive"));

            BigDecimal amount = request.getAmount();

            senderWallet.debitAccount(amount);
            receiverWallet.creditAccount(amount);

            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);
        } catch (Exception e)
        {
            payloadResponse.setIsFailed(true);
            payloadResponse.setIsSucessful(false);
            payloadResponse.setFailedMessage(e.getMessage());

            return payloadResponse;
        }

        return payloadResponse;
    }


}
