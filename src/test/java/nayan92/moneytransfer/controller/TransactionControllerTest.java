package nayan92.moneytransfer.controller;

import nayan92.moneytransfer.controller.mapper.AccountMapper;
import nayan92.moneytransfer.controller.util.LockManager;
import nayan92.moneytransfer.data.exception.AccountNotFoundException;
import nayan92.moneytransfer.data.exception.InsufficientFundsException;
import nayan92.moneytransfer.data.exception.SameAccountTransferException;
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
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionControllerTest {

    @Rule public ExpectedException thrown = ExpectedException.none();
    @Captor ArgumentCaptor<List<BulkUpdate>> updatesCaptor;

    final int accountId1 = 1;
    final int accountId2 = 2;
    @Spy ReentrantLock accountLock1 = new ReentrantLock();
    @Spy ReentrantLock accountLock2 = new ReentrantLock();

    @Mock(lenient = true) AccountDao accountDao;
    @Mock(lenient = true) LockManager lockManager;

    private TransactionController transactionController;

    @Before
    public void setUp() {
        AccountMapper accountMapper = new AccountMapper();
        when(lockManager.getLockForId(accountId1)).thenReturn(accountLock1);
        when(lockManager.getLockForId(accountId2)).thenReturn(accountLock2);
        transactionController = new TransactionController(accountDao, accountMapper, lockManager);
    }

    @Test
    public void transfer_should_throw_exception_if_from_account_does_not_exist() throws AccountNotFoundException, InsufficientFundsException, SameAccountTransferException {
        when(accountDao.getAccountById(accountId1)).thenReturn(Optional.empty());
        when(accountDao.getAccountById(accountId2)).thenReturn(Optional.of(new DbAccount(accountId2, 100)));
        TransferRequest request = transferRequest(accountId1, accountId2, 50);

        thrown.expect(AccountNotFoundException.class);
        thrown.expect(hasProperty("accountId", equalTo(1)));

        transactionController.transfer(request);
    }

    @Test
    public void transfer_should_throw_exception_if_to_account_does_not_exist() throws AccountNotFoundException, InsufficientFundsException, SameAccountTransferException {
        when(accountDao.getAccountById(accountId1)).thenReturn(Optional.of(new DbAccount(accountId1, 100)));
        when(accountDao.getAccountById(accountId2)).thenReturn(Optional.empty());
        TransferRequest request = transferRequest(accountId1, accountId2, 50);

        thrown.expect(AccountNotFoundException.class);
        thrown.expect(hasProperty("accountId", equalTo(2)));

        transactionController.transfer(request);
    }

    @Test
    public void transfer_should_throw_exception_if_from_account_does_not_have_enough_funds() throws AccountNotFoundException, InsufficientFundsException, SameAccountTransferException {
        when(accountDao.getAccountById(accountId1)).thenReturn(Optional.of(new DbAccount(accountId1, 100)));
        when(accountDao.getAccountById(accountId2)).thenReturn(Optional.of(new DbAccount(accountId2, 100)));
        TransferRequest request = transferRequest(accountId1, accountId2, 101);

        thrown.expect(InsufficientFundsException.class);

        transactionController.transfer(request);
    }

    @Test
    public void transfer_should_update_database_if_from_account_has_more_than_enough_funds() throws AccountNotFoundException, InsufficientFundsException, SameAccountTransferException {
        when(accountDao.getAccountById(accountId1)).thenReturn(Optional.of(new DbAccount(accountId1, 100)));
        when(accountDao.getAccountById(accountId2)).thenReturn(Optional.of(new DbAccount(accountId2, 100)));
        TransferRequest request = transferRequest(accountId1, accountId2, 99);

        transactionController.transfer(request);

        verify(accountDao).bulkUpdateBalance(updatesCaptor.capture());
        assertThat(updatesCaptor.getValue(), contains(
            samePropertyValuesAs(new BulkUpdate(accountId1, 1)),
            samePropertyValuesAs(new BulkUpdate(accountId2, 199))
        ));
    }

    @Test
    public void transfer_should_update_database_if_from_account_has_exactly_enough_funds() throws AccountNotFoundException, InsufficientFundsException, SameAccountTransferException {
        when(accountDao.getAccountById(accountId1)).thenReturn(Optional.of(new DbAccount(accountId1, 100)));
        when(accountDao.getAccountById(accountId2)).thenReturn(Optional.of(new DbAccount(accountId2, 100)));
        TransferRequest request = transferRequest(1, 2, 100);

        transactionController.transfer(request);

        verify(accountDao).bulkUpdateBalance(updatesCaptor.capture());
        assertThat(updatesCaptor.getValue(), contains(
                samePropertyValuesAs(new BulkUpdate(accountId1, 0)),
                samePropertyValuesAs(new BulkUpdate(accountId2, 200))
        ));
    }

    @Test
    public void transfer_should_return_the_updated_account_details() throws AccountNotFoundException, InsufficientFundsException, SameAccountTransferException {
        when(accountDao.getAccountById(accountId1)).thenReturn(Optional.of(new DbAccount(accountId1, 100)));
        when(accountDao.getAccountById(accountId2)).thenReturn(Optional.of(new DbAccount(accountId2, 100)));
        TransferRequest request = transferRequest(accountId1, accountId2, 50);

        List<Account> updatedAccounts = transactionController.transfer(request);

        assertThat(updatedAccounts, contains(
            samePropertyValuesAs(expectedAccount(accountId1, 50)),
            samePropertyValuesAs(expectedAccount(accountId2, 150))
        ));
    }

    @Test
    public void transfer_should_throw_exception_if_transferring_between_the_same_account() throws InsufficientFundsException, AccountNotFoundException, SameAccountTransferException {
        TransferRequest request = transferRequest(accountId1, accountId1, 50);

        thrown.expect(SameAccountTransferException.class);

        transactionController.transfer(request);
    }

    @Test
    public void transfer_should_obtain_the_lock_for_from_account_first_if_from_account_id_smaller_than_to_account_id() throws InsufficientFundsException, AccountNotFoundException, SameAccountTransferException {
        int fromAccountId = 50;
        int toAccountId = 100;
        when(accountDao.getAccountById(fromAccountId)).thenReturn(Optional.of(new DbAccount(fromAccountId, 100)));
        when(accountDao.getAccountById(toAccountId)).thenReturn(Optional.of(new DbAccount(toAccountId, 100)));
        when(lockManager.getLockForId(fromAccountId)).thenReturn(accountLock1);
        when(lockManager.getLockForId(toAccountId)).thenReturn(accountLock2);
        TransferRequest request = transferRequest(fromAccountId, toAccountId, 50);

        transactionController.transfer(request);

        InOrder order = inOrder(accountLock1, accountLock2);
        order.verify(accountLock1).lock();
        order.verify(accountLock2).lock();
    }

    @Test
    public void transfer_should_obtain_the_lock_for_to_account_first_if_from_account_id_larger_than_to_account_id() throws InsufficientFundsException, AccountNotFoundException, SameAccountTransferException {
        int fromAccountId = 100;
        int toAccountId = 50;
        when(accountDao.getAccountById(fromAccountId)).thenReturn(Optional.of(new DbAccount(fromAccountId, 100)));
        when(accountDao.getAccountById(toAccountId)).thenReturn(Optional.of(new DbAccount(toAccountId, 100)));
        when(lockManager.getLockForId(fromAccountId)).thenReturn(accountLock1);
        when(lockManager.getLockForId(toAccountId)).thenReturn(accountLock2);
        TransferRequest request = transferRequest(fromAccountId, toAccountId, 50);

        transactionController.transfer(request);

        InOrder order = inOrder(accountLock1, accountLock2);
        order.verify(accountLock2).lock();
        order.verify(accountLock1).lock();
    }

    @Test
    public void transfer_should_unlock_in_the_opposite_order_to_locking() throws InsufficientFundsException, AccountNotFoundException, SameAccountTransferException {
        int fromAccountId = 50;
        int toAccountId = 100;
        when(accountDao.getAccountById(fromAccountId)).thenReturn(Optional.of(new DbAccount(fromAccountId, 100)));
        when(accountDao.getAccountById(toAccountId)).thenReturn(Optional.of(new DbAccount(toAccountId, 100)));
        when(lockManager.getLockForId(fromAccountId)).thenReturn(accountLock1);
        when(lockManager.getLockForId(toAccountId)).thenReturn(accountLock2);
        TransferRequest request = transferRequest(fromAccountId, toAccountId, 50);

        transactionController.transfer(request);

        InOrder order = inOrder(accountLock1, accountLock2);
        order.verify(accountLock2).unlock();
        order.verify(accountLock1).unlock();
    }

    @Test
    public void transfer_should_release_both_locks_even_if_there_is_an_exception() throws InsufficientFundsException, AccountNotFoundException, SameAccountTransferException {
        int fromAccountId = 50;
        int toAccountId = 100;
        when(accountDao.getAccountById(fromAccountId)).thenReturn(Optional.empty());
        when(accountDao.getAccountById(toAccountId)).thenReturn(Optional.empty());
        when(lockManager.getLockForId(fromAccountId)).thenReturn(accountLock1);
        when(lockManager.getLockForId(toAccountId)).thenReturn(accountLock2);
        TransferRequest request = transferRequest(fromAccountId, toAccountId, 50);

        try {
            transactionController.transfer(request);
        } catch (Exception e) {}

        verify(accountLock1).unlock();
        verify(accountLock2).unlock();
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