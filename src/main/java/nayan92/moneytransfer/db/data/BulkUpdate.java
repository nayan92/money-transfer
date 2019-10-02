package nayan92.moneytransfer.db.data;

public class BulkUpdate {

    private final int accountId;
    private final int newBalance;

    public BulkUpdate(int accountId, int newBalance) {
        this.accountId = accountId;
        this.newBalance = newBalance;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getNewBalance() {
        return newBalance;
    }
}
