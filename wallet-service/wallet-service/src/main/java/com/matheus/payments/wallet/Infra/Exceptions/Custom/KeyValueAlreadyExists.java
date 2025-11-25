package com.matheus.payments.wallet.Infra.Exceptions.Custom;

public class KeyValueAlreadyExists extends RuntimeException {
    public KeyValueAlreadyExists(String message) { super(message); }
    public KeyValueAlreadyExists() { super(); }
}
