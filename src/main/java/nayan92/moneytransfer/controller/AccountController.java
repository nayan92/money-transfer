package nayan92.moneytransfer.controller;

import nayan92.moneytransfer.controller.mapper.AccountMapper;
import nayan92.moneytransfer.data.request.NewAccountRequest;
import nayan92.moneytransfer.data.response.Account;
import nayan92.moneytransfer.db.dao.AccountDao;
import nayan92.moneytransfer.db.entity.DbAccount;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class AccountController {

    private final AccountDao accountDao;
    private final AccountMapper accountMapper;

    @Inject
    public AccountController(AccountDao accountDao, AccountMapper accountMapper) {
        this.accountDao = accountDao;
        this.accountMapper = accountMapper;
    }

    public Account createAccount(NewAccountRequest newAccountRequest) {
        int accountId = accountDao.createAccount(newAccountRequest.getBalance());
        return accountMapper.map(new DbAccount(accountId, newAccountRequest.getBalance()));
    }

    public List<Account> getAllAccounts() {
        List<DbAccount> allAccounts = accountDao.getAllAccounts();
        return allAccounts.stream()
                .map(accountMapper::map)
                .collect(Collectors.toList());
    }

    public Account getAccountById(int accountId) {
        DbAccount account = accountDao.getAccountById(accountId).get();
        return accountMapper.map(account);
    }

}
