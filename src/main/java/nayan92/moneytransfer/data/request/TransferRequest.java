package nayan92.moneytransfer.data.request;

public class TransferRequest {

    private long fromAccountId;
    private long toAccountId;
    private long amount;

    public void setFromAccountId(long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public long getFromAccountId() {
        return this.fromAccountId;
    }

    public void setToAccountId(long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public long getToAccountId() {
        return toAccountId;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }
}
