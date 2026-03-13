package com.matheus.payments.UnitTests.Fixtures;

import com.matheus.payments.Application.DTOs.DepositRequest;
import com.matheus.payments.Domain.Models.Deposit;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Fixture class for Deposit-related test data.
 */
public class DepositFixture {

    public static final UUID DEFAULT_RECEIVER_ID = UUID.randomUUID();
    public static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("100.00");

    public static Deposit createDeposit() {
        return new Deposit(DEFAULT_RECEIVER_ID, DEFAULT_AMOUNT);
    }

    public static Deposit createDeposit(UUID receiverId, BigDecimal amount) {
        return new Deposit(receiverId, amount);
    }

    public static DepositRequest createDepositRequest() {
        return new DepositRequest(DEFAULT_RECEIVER_ID, DEFAULT_AMOUNT);
    }

    public static DepositRequest createDepositRequest(UUID receiverId, BigDecimal amount) {
        return new DepositRequest(receiverId, amount);
    }
}

