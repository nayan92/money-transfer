package nayan92.moneytransfer.controller.mapper;

import nayan92.moneytransfer.data.response.Account;
import nayan92.moneytransfer.db.entity.DbAccount;

public class AccountMapper {

    public Account map(DbAccount dbAccount) {
        Account account = new Account();
        account.setAccountId(dbAccount.getAccountId());
        account.setBalance(dbAccount.getBalance());
        return account;
    }

}
