package com.saumesh.payment.persistenance;

public class AccountAlreadyExistsException extends Exception {
    public AccountAlreadyExistsException(String message) {
        this(message, null);
    }

    public AccountAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}