package com.matheus.payments.user_service.Domain.Exceptios;


public class UniqueFieldsEntryViolationException extends DomainException {
    public static final String CODE = "UNIQUE_FIELDS_ENTRY_VIOLATION";
    public static final String MESSAGE = "Unique field violation. The provided value(s) already exist(s) in the system. :";

    public UniqueFieldsEntryViolationException(String errorMessage) {
        super(CODE, String.format(MESSAGE).concat(errorMessage));
    }
}
