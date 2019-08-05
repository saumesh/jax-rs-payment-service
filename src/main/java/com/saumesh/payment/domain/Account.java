package com.saumesh.payment.domain;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties(value = {"lock", "readLock", "writeLock"})
public class Account {
    private static final Logger logger = LoggerFactory.getLogger(Account.class);

    private final ReadWriteLock lock;
    private final Lock readLock;
    private final Lock writeLock;

    @JsonProperty
    private final long accountNumber;
    @JsonProperty
    private volatile double balance;

    @JsonCreator
    public Account(@JsonProperty("accountNumber") long accountNumber,
                   @JsonProperty("balance") double balance) {
        this.lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();

        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public long accountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        readLock.lock();
        try {
            return balance;
        } finally {
            readLock.unlock();
        }
    }

    public void setBalance(double balance) {
        writeLock.lock();
        try {
            this.balance = balance;
        } finally {
            writeLock.unlock();
        }
    }

    public double add(double amount) {
        writeLock.lock();
        try {
            balance += amount;
            logger.info("Added {} to account: {}", amount, accountNumber);
            return balance;
        } finally {
            writeLock.unlock();
        }
    }

    public double deduct(double amount) throws InsufficientBalanceException {
        writeLock.lock();
        try {
            if (balance < amount) {
                logger.warn("Insufficient balance {} to deduct {} from account: {}", balance, amount, accountNumber);
                throw new InsufficientBalanceException("Insufficient balance " + balance);
            }

            balance -= amount;
            logger.info("Deducted {} from account: {}", amount, accountNumber);
            return balance;
        } finally {
            writeLock.unlock();
        }
    }
}
