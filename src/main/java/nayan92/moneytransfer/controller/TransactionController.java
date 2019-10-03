package nayan92.moneytransfer.controller;

import nayan92.moneytransfer.controller.mapper.AccountMapper;
import nayan92.moneytransfer.controller.util.LockManager;
import nayan92.moneytransfer.data.exception.AccountNotFoundException;
import nayan92.moneytransfer.data.exception.InsufficientFundsException;
import nayan92.moneytransfer.data.exception.SameAccountTransferException;
import nayan92.moneytransfer.data.request.TransferRequest;
import nayan92.moneytransfer.data.response.Account;
import nayan92.moneytransfer.db.dao.AccountDao;
import nayan92.moneytransfer.db.data.BulkUpdate;
import nayan92.moneytransfer.db.entity.DbAccount;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class TransactionController {

    private final AccountDao accountDao;
    private final AccountMapper accountMapper;
    private final LockManager lockManager;

    @Inject
    public TransactionController(AccountDao accountDao, AccountMapper accountMapper, LockManager lockManager) {
        this.accountDao = accountDao;
        this.accountMapper = accountMapper;
        this.lockManager = lockManager;
    }

    public List<Account> transfer(TransferRequest transferRequest) throws SameAccountTransferException, AccountNotFoundException, InsufficientFundsException {
        Lock firstLock;
        Lock secondLock;
        if (transferRequest.getFromAccountId() == transferRequest.getToAccountId()) {
            // TODO handle exception
            throw new SameAccountTransferException();
        } else if (transferRequest.getFromAccountId() < transferRequest.getToAccountId()) {
            firstLock = lockManager.getLockForId(transferRequest.getFromAccountId());
            secondLock = lockManager.getLockForId(transferRequest.getToAccountId());
        } else {
            firstLock = lockManager.getLockForId(transferRequest.getToAccountId());
            secondLock = lockManager.getLockForId(transferRequest.getFromAccountId());
        }

        firstLock.lock();
        secondLock.lock();
        try {
            List<DbAccount> updatedAccounts = performTransferReturningUpdatedAccounts(transferRequest);
            return updatedAccounts.stream()
                    .map(accountMapper::map)
                    .collect(Collectors.toList());
        } finally {
            secondLock.unlock();
            firstLock.unlock();
        }
    }

    private List<DbAccount> performTransferReturningUpdatedAccounts(TransferRequest transferRequest) throws AccountNotFoundException, InsufficientFundsException {
        DbAccount fromAccount = getAccountFromDb(transferRequest.getFromAccountId());
        DbAccount toAccount = getAccountFromDb(transferRequest.getToAccountId());

        int fromAccountNewBalance = fromAccount.getBalance() - transferRequest.getAmount();
        int toAccountNewBalance = toAccount.getBalance() + transferRequest.getAmount();

        if (fromAccountNewBalance < 0) {
            throw new InsufficientFundsException();
        }

        BulkUpdate fromAccountUpdate = new BulkUpdate(fromAccount.getAccountId(), fromAccountNewBalance);
        BulkUpdate toAccountUpdate = new BulkUpdate(toAccount.getAccountId(), toAccountNewBalance);
        accountDao.bulkUpdateBalance(asList(fromAccountUpdate, toAccountUpdate));

        DbAccount newFromAccount = new DbAccount(fromAccount.getAccountId(), fromAccountNewBalance);
        DbAccount newToAccount = new DbAccount(toAccount.getAccountId(), toAccountNewBalance);
        return asList(newFromAccount, newToAccount);
    }

    private DbAccount getAccountFromDb(int accountId) throws AccountNotFoundException {
        return accountDao.getAccountById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
    }

}
