package nayan92.moneytransfer.controller;

import nayan92.moneytransfer.controller.mapper.AccountMapper;
import nayan92.moneytransfer.data.request.TransferRequest;
import nayan92.moneytransfer.data.response.Account;
import nayan92.moneytransfer.db.dao.AccountDAO;
import nayan92.moneytransfer.db.data.BulkUpdate;
import nayan92.moneytransfer.db.entity.DbAccount;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class TransactionController {

    private final AccountDAO accountDAO;
    private final AccountMapper accountMapper;

    public TransactionController(AccountDAO accountDAO, AccountMapper accountMapper) {
        this.accountDAO = accountDAO;
        this.accountMapper = accountMapper;
    }

    public List<Account> transfer(TransferRequest transferRequest) {
        DbAccount fromAccount = accountDAO.getAccountById(transferRequest.getFromAccountId());
        DbAccount toAccount = accountDAO.getAccountById(transferRequest.getToAccountId());

        int fromAccountNewBalance = fromAccount.getBalance() - transferRequest.getAmount();
        int toAccountNewBalance = toAccount.getBalance() + transferRequest.getAmount();

        BulkUpdate fromAccountUpdate = new BulkUpdate(fromAccount.getAccountId(), fromAccountNewBalance);
        BulkUpdate toAccountUpdate = new BulkUpdate(toAccount.getAccountId(), toAccountNewBalance);

        accountDAO.bulkUpdateBalance(asList(fromAccountUpdate, toAccountUpdate));

        DbAccount newFromAccount = accountDAO.getAccountById(transferRequest.getFromAccountId());
        DbAccount newToAccount = accountDAO.getAccountById(transferRequest.getToAccountId());

        return Stream.of(newFromAccount, newToAccount)
                .map(accountMapper::map)
                .collect(Collectors.toList());
    }

}
