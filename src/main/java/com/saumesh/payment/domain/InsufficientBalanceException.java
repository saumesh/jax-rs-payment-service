package com.saumesh.payment.domain;

public class InsufficientBalanceException extends Exception {

    public InsufficientBalanceException(String message) {
        this(message, null);
    }

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
