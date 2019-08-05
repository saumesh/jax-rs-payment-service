package com.saumesh.payment.controller;

import com.saumesh.payment.domain.Account;
import com.saumesh.payment.domain.InsufficientBalanceException;
import com.saumesh.payment.persistenance.AccountNotFoundException;
import com.saumesh.payment.persistenance.AccountRepository;
import com.saumesh.payment.persistenance.memory.InMemoryAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class BasicPaymentController implements PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(BasicPaymentController.class);
    private AccountRepository accountRepository;

    @Inject
    public BasicPaymentController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;

        // Initialize with test accounts
        Map<Long, Double> accounts = new HashMap<>();
        accounts.put(123L, 100.0);
        accounts.put(124L, 100.0);
        accounts.put(125L, 100.0);
        accounts.put(1234L, 200.0);
        accounts.put(1235L, 200.0);
        accounts.put(1236L, 200.0);
        accounts.put(12345L, 300.0);
        accounts.put(12346L, 300.0);
        accounts.put(123456L, 400.0);
        accounts.put(123457L, 400.0);
        ((InMemoryAccountRepository)this.accountRepository).initialize(accounts);
    }

    @Override
    public Optional<Account> getAccount(long accountNumber) {
        logger.debug("Fetching account details of {} ", accountNumber);
        try {
            return Optional.ofNullable(accountRepository.get(accountNumber));
        } catch (AccountNotFoundException excp) {
            logger.warn("No account found with accountNumber: {}", accountNumber);
            return Optional.empty();
        }
    }

    @Override
    public boolean transfer(Account source, Account target, double amount) throws InsufficientBalanceException {
        if(null == source || null == target || 0 >= amount) {
            throw new IllegalArgumentException("Invalid Source | Target account OR invalid amount");
        }

        // Kind of transaction
        synchronized (source) {
            boolean isFailed = false;
            double sourceAmount = source.getBalance();
            double targetAmount = target.getBalance();
            try {
                source.deduct(amount);
                target.add(amount);
                if(accountRepository.update(source) && accountRepository.update(target)) {
                    return true;
                }

                // Control reaches here only when update fails.
                isFailed = true;
            } catch (AccountNotFoundException excp) {
                logger.warn("Error in transferring amount from {} to {}", source.accountNumber(), target.accountNumber(), excp);
                isFailed = true;
            }

            if(isFailed) {
                //Rollback/reset to original amount & update in repository
                try {
                    source.setBalance(sourceAmount);
                    target.setBalance(targetAmount);
                    accountRepository.update(source);
                    accountRepository.update(target);
                } catch (Exception excp) {
                    logger.debug("Error in rolling back", excp);
                }
            }
        }
        return false;
    }
}
