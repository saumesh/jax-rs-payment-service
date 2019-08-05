package com.saumesh.payment.domain;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountTest {

    @Test
    public void testAdd() {
        double balance = 100;
        Account account = new Account(1234, balance);

        double amount = 50;
        account.add(amount);
        assertThat(account.getBalance()).isEqualTo(balance+amount);
    }

    @Test
    public void testDeduct() throws InsufficientBalanceException {
        double balance = 100;
        Account account = new Account(1234, balance);

        double amount = 50;
        account.deduct(amount);
        assertThat(account.getBalance()).isEqualTo(balance-amount);
    }

    @Test(expected = InsufficientBalanceException.class)
    public void testDeductInsufficientBalance() throws InsufficientBalanceException {
        double balance = 100;
        Account account = new Account(1234, balance);

        double amount = 150;
        account.deduct(amount);
        assertThat(account.getBalance()).isEqualTo(balance-amount);
    }
}
