package com.matheus.payments.wallet.Application.DTOs.Context;

import com.matheus.payments.wallet.Domain.Models.Wallet;

import java.math.BigDecimal;

/**
 * This class was created to encapsulate the data required for a wallet transfer operation.
 * When using Optimistic Locking, it's essential to have all necessary information bundled together
 * to ensure data consistency and integrity during concurrent transactions.
 *
 */
public record WalletTransfer(Wallet senderWallet, Wallet receiverWallet, BigDecimal amount) {
}