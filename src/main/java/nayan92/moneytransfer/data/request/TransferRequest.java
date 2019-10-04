package nayan92.moneytransfer.data.request;

public class TransferRequest {

    private int fromAccountId;
    private int toAccountId;
    private int amount;

    public void setFromAccountId(int fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public int getFromAccountId() {
        return this.fromAccountId;
    }

    public void setToAccountId(int toAccountId) {
        this.toAccountId = toAccountId;
    }

    public int getToAccountId() {
        return toAccountId;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }


}
