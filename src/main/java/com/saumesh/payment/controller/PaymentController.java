package com.saumesh.payment.controller;

import com.saumesh.payment.domain.Account;
import com.saumesh.payment.domain.InsufficientBalanceException;

import java.util.Optional;

public interface PaymentController {
    Optional<Account> getAccount(long accountNumber);
    boolean transfer(Account source, Account target, double amount) throws InsufficientBalanceException;
}
