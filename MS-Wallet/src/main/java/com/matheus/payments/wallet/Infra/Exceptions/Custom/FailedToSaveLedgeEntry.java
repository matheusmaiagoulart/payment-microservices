package com.matheus.payments.wallet.Infra.Exceptions.Custom;

import java.util.UUID;

public class FailedToSaveLedgeEntry extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "Failed to save ledger entry for transaction: ";

    public FailedToSaveLedgeEntry(String transactionId) {
        super(DEFAULT_MESSAGE + transactionId);
    }
}
