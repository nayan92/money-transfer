package nayan92.moneytransfer.controller;

import nayan92.moneytransfer.controller.mapper.AccountMapper;
import nayan92.moneytransfer.data.exception.AccountNotFoundException;
import nayan92.moneytransfer.data.exception.InsufficientFundsException;
import nayan92.moneytransfer.data.request.TransferRequest;
import nayan92.moneytransfer.data.response.Account;
import nayan92.moneytransfer.db.dao.AccountDao;
import nayan92.moneytransfer.db.data.BulkUpdate;
import nayan92.moneytransfer.db.entity.DbAccount;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionControllerTest {

    @Rule public ExpectedException thrown = ExpectedException.none();
    @Captor ArgumentCaptor<List<BulkUpdate>> updatesCaptor;

    @Mock(lenient = true) AccountDao accountDao;

    private TransactionController transactionController;

    @Before
    public void setUp() {
        AccountMapper accountMapper = new AccountMapper();
        transactionController = new TransactionController(accountDao, accountMapper);
    }

    @Test
    public void transfer_should_throw_exception_if_from_account_does_not_exist() throws AccountNotFoundException, InsufficientFundsException {
        when(accountDao.getAccountById(1)).thenReturn(Optional.empty());
        when(accountDao.getAccountById(2)).thenReturn(Optional.of(new DbAccount(2, 100)));
        TransferRequest request = transferRequest(1, 2, 50);

        thrown.expect(AccountNotFoundException.class);
        thrown.expect(hasProperty("accountId", equalTo(1)));

        transactionController.transfer(request);
    }

    @Test
    public void transfer_should_throw_exception_if_to_account_does_not_exist() throws AccountNotFoundException, InsufficientFundsException {
        when(accountDao.getAccountById(1)).thenReturn(Optional.of(new DbAccount(1, 100)));
        when(accountDao.getAccountById(2)).thenReturn(Optional.empty());
        TransferRequest request = transferRequest(1, 2, 50);

        thrown.expect(AccountNotFoundException.class);
        thrown.expect(hasProperty("accountId", equalTo(2)));

        transactionController.transfer(request);
    }

    @Test
    public void transfer_should_throw_exception_if_from_account_does_not_have_enough_funds() throws AccountNotFoundException, InsufficientFundsException {
        when(accountDao.getAccountById(1)).thenReturn(Optional.of(new DbAccount(1, 100)));
        when(accountDao.getAccountById(2)).thenReturn(Optional.of(new DbAccount(2, 100)));
        TransferRequest request = transferRequest(1, 2, 101);

        thrown.expect(InsufficientFundsException.class);

        transactionController.transfer(request);
    }

    @Test
    public void transfer_should_update_database_if_from_account_has_more_than_enough_funds() throws AccountNotFoundException, InsufficientFundsException {
        when(accountDao.getAccountById(1)).thenReturn(Optional.of(new DbAccount(1, 100)));
        when(accountDao.getAccountById(2)).thenReturn(Optional.of(new DbAccount(2, 100)));
        TransferRequest request = transferRequest(1, 2, 99);

        transactionController.transfer(request);

        verify(accountDao).bulkUpdateBalance(updatesCaptor.capture());
        assertThat(updatesCaptor.getValue(), contains(
            samePropertyValuesAs(new BulkUpdate(1, 1)),
            samePropertyValuesAs(new BulkUpdate(2, 199))
        ));
    }

    @Test
    public void transfer_should_update_database_if_from_account_has_exactly_enough_funds() throws AccountNotFoundException, InsufficientFundsException {
        when(accountDao.getAccountById(1)).thenReturn(Optional.of(new DbAccount(1, 100)));
        when(accountDao.getAccountById(2)).thenReturn(Optional.of(new DbAccount(2, 100)));
        TransferRequest request = transferRequest(1, 2, 100);

        transactionController.transfer(request);

        verify(accountDao).bulkUpdateBalance(updatesCaptor.capture());
        assertThat(updatesCaptor.getValue(), contains(
                samePropertyValuesAs(new BulkUpdate(1, 0)),
                samePropertyValuesAs(new BulkUpdate(2, 200))
        ));
    }

    @Test
    public void transfer_should_return_the_updated_account_details() throws AccountNotFoundException, InsufficientFundsException {
        when(accountDao.getAccountById(1)).thenReturn(Optional.of(new DbAccount(1, 100)));
        when(accountDao.getAccountById(2)).thenReturn(Optional.of(new DbAccount(2, 100)));
        TransferRequest request = transferRequest(1, 2, 50);

        List<Account> updatedAccounts = transactionController.transfer(request);

        assertThat(updatedAccounts, contains(
            samePropertyValuesAs(expectedAccount(1, 50)),
            samePropertyValuesAs(expectedAccount(2, 150))
        ));
    }

    private TransferRequest transferRequest(int fromAccountId, int toAccountId, int amount) {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(fromAccountId);
        request.setToAccountId(toAccountId);
        request.setAmount(amount);
        return request;
    }

    private Account expectedAccount(int accountId, int balance) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setBalance(balance);
        return account;
    }

}