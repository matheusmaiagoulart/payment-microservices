package com.matheus.payments.wallet.Infra.Exceptions.Custom;

public class FailedToSaveLedgerEntry extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "Failed to save ledger entry for transaction: ";

    public FailedToSaveLedgerEntry(String transactionId) {
        super(DEFAULT_MESSAGE + transactionId);
    }
}
