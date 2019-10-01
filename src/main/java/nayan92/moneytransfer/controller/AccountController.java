package nayan92.moneytransfer.controller;

import nayan92.moneytransfer.controller.mapper.AccountMapper;
import nayan92.moneytransfer.data.request.NewAccountRequest;
import nayan92.moneytransfer.data.response.Account;
import nayan92.moneytransfer.db.dao.AccountDAO;
import nayan92.moneytransfer.db.entity.DbAccount;

import java.util.List;
import java.util.stream.Collectors;

public class AccountController {

    private final AccountDAO accountDAO;
    private final AccountMapper accountMapper;

    public AccountController(AccountDAO accountDAO, AccountMapper accountMapper) {
        this.accountDAO = accountDAO;
        this.accountMapper = accountMapper;
    }

    public Account createAccount(NewAccountRequest newAccountRequest) {
        long accountId = accountDAO.createAccount(newAccountRequest.getBalance());
        return accountMapper.map(new DbAccount(accountId, newAccountRequest.getBalance()));
    }

    public List<Account> getAllAccounts() {
        List<DbAccount> allAccounts = accountDAO.getAllAccounts();
        return allAccounts.stream()
                .map(accountMapper::map)
                .collect(Collectors.toList());
    }

    public Account getAccountById(long accountId) {
        DbAccount account = accountDAO.getAccountById(accountId);
        return accountMapper.map(account);
    }

}
