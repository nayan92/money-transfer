package nayan92.moneytransfer.db.entity;

public class DbAccount {

    private final long accountId;
    private final long balance;

    public DbAccount(long accountId, long balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getBalance() {
        return balance;
    }

}
