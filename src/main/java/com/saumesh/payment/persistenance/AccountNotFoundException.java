package com.saumesh.payment.persistenance;

public class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String message) {
        this(message, null);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
