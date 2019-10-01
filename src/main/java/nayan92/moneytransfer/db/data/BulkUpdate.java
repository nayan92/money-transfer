package nayan92.moneytransfer.db.data;

public class BulkUpdate {

    private final long accountId;
    private final long newBalance;

    public BulkUpdate(long accountId, long newBalance) {
        this.accountId = accountId;
        this.newBalance = newBalance;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getNewBalance() {
        return newBalance;
    }
}
