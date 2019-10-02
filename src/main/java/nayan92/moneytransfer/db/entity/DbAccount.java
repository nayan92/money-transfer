package nayan92.moneytransfer.db.entity;

public class DbAccount {

    private final int accountId;
    private final int balance;

    public DbAccount(int accountId, int balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getBalance() {
        return balance;
    }

}
