package com.saumesh.payment.persistenance.memory;

import com.saumesh.payment.domain.Account;
import com.saumesh.payment.persistenance.AccountAlreadyExistsException;
import com.saumesh.payment.persistenance.AccountRepository;
import com.saumesh.payment.persistenance.AccountNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InMemoryAccountRepository implements AccountRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryAccountRepository.class);
    private final Map<Long, Account> accounts;  // In-Memory store to hold accounts details

    public InMemoryAccountRepository() {
        accounts = new ConcurrentHashMap<>();
    }

    public void initialize(Map<Long, Double> accounts) {
        if(null != accounts) {
            accounts.forEach((a, b) -> {
                try {
                    create(new Account(a, b));
                } catch (AccountAlreadyExistsException excp) {
                    logger.debug("Duplicate account detected during initialization");
                }
            });
        }
    }

    @Override
    public Account get(long accountNumber) throws AccountNotFoundException {
        Account account = accounts.get(accountNumber);
        if(null == account) {
            logger.info("No account found with accountNumber: {}", accountNumber);
            throw new AccountNotFoundException("No account found with AccountNumber " + accountNumber);
        }
        return account;
    }

    @Override
    public boolean create(Account account) throws AccountAlreadyExistsException {
        if(null != accounts.get(account.accountNumber())) {
            logger.info("Account already exists with accountNumber: {}", account.accountNumber());
            throw new AccountAlreadyExistsException("Account already exists with AccountNumber " + account.accountNumber());
        }
        return null != accounts.put(account.accountNumber(), account);
    }

    @Override
    public boolean update(Account account) throws AccountNotFoundException {
        if(null == accounts.get(account.accountNumber())) {
            logger.info("No account found with accountNumber: {}", account.accountNumber());
            throw new AccountNotFoundException("No account found with AccountNumber " + account.accountNumber());
        }
        return null != accounts.put(account.accountNumber(), account);
    }
}
