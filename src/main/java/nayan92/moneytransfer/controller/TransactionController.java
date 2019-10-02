package nayan92.moneytransfer.controller;

import nayan92.moneytransfer.controller.mapper.AccountMapper;
import nayan92.moneytransfer.data.request.TransferRequest;
import nayan92.moneytransfer.data.response.Account;
import nayan92.moneytransfer.db.dao.AccountDao;
import nayan92.moneytransfer.db.data.BulkUpdate;
import nayan92.moneytransfer.db.entity.DbAccount;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class TransactionController {

    private final AccountDao accountDao;
    private final AccountMapper accountMapper;

    public TransactionController(AccountDao accountDao, AccountMapper accountMapper) {
        this.accountDao = accountDao;
        this.accountMapper = accountMapper;
    }

    public List<Account> transfer(TransferRequest transferRequest) {
        DbAccount fromAccount = accountDao.getAccountById(transferRequest.getFromAccountId()).get();
        DbAccount toAccount = accountDao.getAccountById(transferRequest.getToAccountId()).get();

        int fromAccountNewBalance = fromAccount.getBalance() - transferRequest.getAmount();
        int toAccountNewBalance = toAccount.getBalance() + transferRequest.getAmount();

        BulkUpdate fromAccountUpdate = new BulkUpdate(fromAccount.getAccountId(), fromAccountNewBalance);
        BulkUpdate toAccountUpdate = new BulkUpdate(toAccount.getAccountId(), toAccountNewBalance);

        accountDao.bulkUpdateBalance(asList(fromAccountUpdate, toAccountUpdate));

        DbAccount newFromAccount = accountDao.getAccountById(transferRequest.getFromAccountId()).get();
        DbAccount newToAccount = accountDao.getAccountById(transferRequest.getToAccountId()).get();

        return Stream.of(newFromAccount, newToAccount)
                .map(accountMapper::map)
                .collect(Collectors.toList());
    }

}
