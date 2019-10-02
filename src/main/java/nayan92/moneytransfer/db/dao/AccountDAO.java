package nayan92.moneytransfer.db.dao;

import nayan92.moneytransfer.db.JdbiProvider;
import nayan92.moneytransfer.db.data.BulkUpdate;
import nayan92.moneytransfer.db.entity.DbAccount;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class AccountDao {

    private final JdbiProvider dbProvider;

    @Inject
    public AccountDao(JdbiProvider dbProvider) {
        this.dbProvider = dbProvider;
    }

    public int createAccount(int balance) {
        return dbProvider.get().withHandle(handle -> {
            return handle.createUpdate("insert into account (balance) values (:balance)")
                    .bind("balance", balance)
                    .executeAndReturnGeneratedKeys("account_id")
                    .mapTo(Integer.class)
                    .one();
        });
    }

    public List<DbAccount> getAllAccounts() {
        return dbProvider.get().withHandle(handle -> {
            return handle.createQuery("select * from account")
                    .mapTo(DbAccount.class)
                    .list();
        });
    }

    public Optional<DbAccount> getAccountById(int accountId) {
        return dbProvider.get().withHandle(handle -> {
            return handle.createQuery("select * from account where account_id = :accountId")
                    .bind("accountId", accountId)
                    .mapTo(DbAccount.class)
                    .findOne();
        });
    }

    public void bulkUpdateBalance(List<BulkUpdate> updates) {
        dbProvider.get().useTransaction(handle -> {
            updates.forEach(update -> {
                handle.createUpdate("update account set balance = :balance where account_id = :accountId")
                        .bind("balance", update.getNewBalance())
                        .bind("accountId", update.getAccountId())
                        .execute();
            });
        });
    }
}
