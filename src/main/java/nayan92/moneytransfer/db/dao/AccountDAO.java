package nayan92.moneytransfer.db.dao;

import nayan92.moneytransfer.db.JdbiProvider;
import nayan92.moneytransfer.db.data.BulkUpdate;
import nayan92.moneytransfer.db.entity.DbAccount;

import java.util.List;

public class AccountDAO {

    private final JdbiProvider dbProvider;

    public AccountDAO(JdbiProvider dbProvider) {
        this.dbProvider = dbProvider;
    }

    public long createAccount(long balance) {
        return dbProvider.get().withHandle(handle -> {
            return handle.createUpdate("insert into account (balance) values (:balance)")
                    .bind("balance", balance)
                    .executeAndReturnGeneratedKeys("account_id")
                    .mapTo(Long.class)
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

    public DbAccount getAccountById(long accountId) {
        return dbProvider.get().withHandle(handle -> {
            return handle.createQuery("select * from account where account_id = :accountId")
                    .bind("accountId", accountId)
                    .mapTo(DbAccount.class)
                    .one();
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
