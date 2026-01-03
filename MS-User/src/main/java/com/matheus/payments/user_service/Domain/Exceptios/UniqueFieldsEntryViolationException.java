package com.matheus.payments.user_service.Domain.Exceptios;

import java.util.List;

public class UniqueFieldsEntryViolationException extends RuntimeException {
    public UniqueFieldsEntryViolationException(String message) {
        super(message);
    }
    public UniqueFieldsEntryViolationException(List<String> errors) {
        super(errors.toString());
    }
}
