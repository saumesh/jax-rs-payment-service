package com.saumesh.payment.persistenance;

import com.saumesh.payment.domain.Account;

public interface AccountRepository {
    Account get(long accountNumber) throws AccountNotFoundException;
    boolean create(Account account) throws AccountAlreadyExistsException;
    boolean update(Account account) throws AccountNotFoundException;
}
