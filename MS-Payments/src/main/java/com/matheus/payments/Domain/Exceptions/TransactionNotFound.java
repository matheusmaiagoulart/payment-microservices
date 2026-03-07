package com.matheus.payments.Domain.Exceptions;

import org.apache.kafka.common.protocol.types.Field;

public class TransactionNotFound extends DomainException {
    public static final String CODE = "TRANSACTION_NOT_FOUND";
    public static final String MESSAGE = "Transaction with ID %s not found.";

    public TransactionNotFound(String transactionId) {
        super(CODE, String.format(MESSAGE, transactionId));
    }
}
