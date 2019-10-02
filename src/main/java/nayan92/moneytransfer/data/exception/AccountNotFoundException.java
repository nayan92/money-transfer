package nayan92.moneytransfer.data.exception;

public class AccountNotFoundException extends Exception {

    private final int accountId;

    public AccountNotFoundException(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }
}
