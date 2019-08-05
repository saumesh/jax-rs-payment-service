package com.saumesh.payment.controller;

import com.saumesh.payment.domain.Account;
import com.saumesh.payment.domain.InsufficientBalanceException;
import com.saumesh.payment.persistenance.AccountRepository;
import com.saumesh.payment.persistenance.AccountNotFoundException;
import com.saumesh.payment.persistenance.memory.InMemoryAccountRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

public class BasicPaymentControllerTest {
    private AccountRepository accountRepository;
    private PaymentController paymentController;

    @Before
    public void setup(){
        accountRepository = mock(InMemoryAccountRepository.class);
        paymentController = new BasicPaymentController(accountRepository);
    }

    @Test
    public void testGetAccount() throws AccountNotFoundException {
        long accountNumber = 123455L;
        double balance = 450.45;
        Account account = new Account(accountNumber, balance);

        when(accountRepository.get(accountNumber)).thenReturn(account);

        Optional<Account> fetchedAccountOpt = paymentController.getAccount(accountNumber);
        assertThat(fetchedAccountOpt).isNotNull();
        assertThat(fetchedAccountOpt).isNotEmpty();
        assertThat(fetchedAccountOpt).isPresent();

        Account fetchedAccount = fetchedAccountOpt.get();
        assertThat(fetchedAccount).isNotNull();
        assertThat(fetchedAccount.accountNumber()).isEqualTo(accountNumber);
        assertThat(fetchedAccount.getBalance()).isEqualTo(balance);
    }

    @Test
    public void getGetAccountNoAccount() throws AccountNotFoundException {
        long accountNumber = 123455L;

        when(accountRepository.get(accountNumber)).thenReturn(null);

        Optional<Account> fetchedAccountOpt = paymentController.getAccount(accountNumber);
        assertThat(fetchedAccountOpt).isNotNull();
        assertThat(fetchedAccountOpt).isEmpty();
        assertThat(fetchedAccountOpt).isNotPresent();
    }

    @Test
    public void getGetAccountNotFound() throws AccountNotFoundException {
        long accountNumber = 123455L;

        when(accountRepository.get(accountNumber)).thenThrow(new AccountNotFoundException("No account found with accountNumber: " + accountNumber));

        Optional<Account> fetchedAccountOpt = paymentController.getAccount(accountNumber);
        assertThat(fetchedAccountOpt).isNotNull();
        assertThat(fetchedAccountOpt).isEmpty();
        assertThat(fetchedAccountOpt).isNotPresent();
    }

    @Test
    public void testTransfer() throws AccountNotFoundException, InsufficientBalanceException {
        long srcAccNumber = 1234;
        double srcAccBalance = 200;
        Account srcAccount = new Account(srcAccNumber, srcAccBalance);

        long tarAccNumber = 65452;
        double tarAccBalance = 140;
        Account tarAccount = new Account(tarAccNumber, tarAccBalance);

        double amount = 25;

        when(accountRepository.update(any(Account.class))).thenReturn(true);

        boolean status = paymentController.transfer(srcAccount, tarAccount, amount);
        assertThat(status).isTrue();
        assertThat(srcAccount.getBalance()).isEqualTo(srcAccBalance-amount);
        assertThat(tarAccount.getBalance()).isEqualTo(tarAccBalance+amount);
    }

    @Test
    public void testTransferUpdateAccountsFails() throws Exception {
        long srcAccNumber = 1234;
        double srcAccBalance = 200;
        Account srcAccount = new Account(srcAccNumber, srcAccBalance);

        long tarAccNumber = 65452;
        double tarAccBalance = 140;
        Account tarAccount = new Account(tarAccNumber, tarAccBalance);

        double amount = 25;

        when(accountRepository.update(any(Account.class))).thenReturn(false);

        boolean status = paymentController.transfer(srcAccount, tarAccount, amount);
        assertThat(status).isFalse();
        assertThat(srcAccount.getBalance()).isEqualTo(srcAccBalance);
        assertThat(tarAccount.getBalance()).isEqualTo(tarAccBalance);
    }

    @Test
    public void testTransferUpdateSourceAccountFails() throws Exception {
        long srcAccNumber = 1234;
        double srcAccBalance = 200;
        Account srcAccount = new Account(srcAccNumber, srcAccBalance);

        long tarAccNumber = 65452;
        double tarAccBalance = 140;
        Account tarAccount = new Account(tarAccNumber, tarAccBalance);

        double amount = 25;

        when(accountRepository.update(srcAccount)).thenReturn(false);
        when(accountRepository.update(tarAccount)).thenReturn(true);

        boolean status = paymentController.transfer(srcAccount, tarAccount, amount);
        assertThat(status).isFalse();
        assertThat(srcAccount.getBalance()).isEqualTo(srcAccBalance);
        assertThat(tarAccount.getBalance()).isEqualTo(tarAccBalance);
    }

    @Test
    public void testTransferUpdateTargetAccountFails() throws Exception {
        long srcAccNumber = 1234;
        double srcAccBalance = 200;
        Account srcAccount = new Account(srcAccNumber, srcAccBalance);

        long tarAccNumber = 65452;
        double tarAccBalance = 140;
        Account tarAccount = new Account(tarAccNumber, tarAccBalance);

        double amount = 25;

        when(accountRepository.update(srcAccount)).thenReturn(true);
        when(accountRepository.update(tarAccount)).thenReturn(false);

        boolean status = paymentController.transfer(srcAccount, tarAccount, amount);
        assertThat(status).isFalse();
        assertThat(srcAccount.getBalance()).isEqualTo(srcAccBalance);
        assertThat(tarAccount.getBalance()).isEqualTo(tarAccBalance);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferInvalidSourceAccount() throws Exception {
        long tarAccNumber = 65452;
        double tarAccBalance = 140;
        Account tarAccount = new Account(tarAccNumber, tarAccBalance);

        double amount = 25;

        when(accountRepository.update(any(Account.class))).thenReturn(false);

        paymentController.transfer(null, tarAccount, amount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferInvalidTargetAccount() throws Exception {
        long srcAccNumber = 1234;
        double srcAccBalance = 200;
        Account srcAccount = new Account(srcAccNumber, srcAccBalance);

        double amount = 25;

        when(accountRepository.update(any(Account.class))).thenReturn(false);

        paymentController.transfer(srcAccount, null, amount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferNegativeAmount() throws Exception {
        long srcAccNumber = 1234;
        double srcAccBalance = 200;
        Account srcAccount = new Account(srcAccNumber, srcAccBalance);

        long tarAccNumber = 65452;
        double tarAccBalance = 140;
        Account tarAccount = new Account(tarAccNumber, tarAccBalance);

        double amount = -25;

        when(accountRepository.update(any(Account.class))).thenReturn(false);

        paymentController.transfer(srcAccount, tarAccount, amount);
    }

    @Test(expected = InsufficientBalanceException.class)
    public void testTransferInsufficientBalance() throws Exception {
        long srcAccNumber = 1234;
        double srcAccBalance = 200;
        Account srcAccount = new Account(srcAccNumber, srcAccBalance);

        long tarAccNumber = 65452;
        double tarAccBalance = 140;
        Account tarAccount = new Account(tarAccNumber, tarAccBalance);

        double amount = 500;

        when(accountRepository.update(srcAccount)).thenReturn(true);
        when(accountRepository.update(tarAccount)).thenReturn(false);

        paymentController.transfer(srcAccount, tarAccount, amount);
    }
}
