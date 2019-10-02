package nayan92.moneytransfer.controller;

import nayan92.moneytransfer.controller.mapper.AccountMapper;
import nayan92.moneytransfer.data.exception.AccountNotFoundException;
import nayan92.moneytransfer.data.request.NewAccountRequest;
import nayan92.moneytransfer.data.response.Account;
import nayan92.moneytransfer.db.dao.AccountDao;
import nayan92.moneytransfer.db.entity.DbAccount;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Mock AccountDao accountDao;

    private AccountController accountController;

    @Before
    public void setUp() {
        AccountMapper accountMapper = new AccountMapper();
        accountController = new AccountController(accountDao, accountMapper);
    }

    @Test
    public void creating_a_new_account_should_return_the_new_account_details() {
        NewAccountRequest request = newAccountRequestWithBalance(50);
        when(accountDao.createAccount(50)).thenReturn(10);

        Account newAccount = accountController.createAccount(request);

        Account expectedAccount = expectedAccount(10, 50);
        assertThat(newAccount, samePropertyValuesAs(expectedAccount));
    }

    @Test
    public void getting_all_accounts_should_return_all_accounts() {
        when(accountDao.getAllAccounts()).thenReturn(asList(
            new DbAccount(1, 10),
            new DbAccount(2, 20),
            new DbAccount(3, 30)
        ));

        List<Account> accounts = accountController.getAllAccounts();

        assertThat(accounts, contains(
            samePropertyValuesAs(expectedAccount(1, 10)),
            samePropertyValuesAs(expectedAccount(2, 20)),
            samePropertyValuesAs(expectedAccount(3, 30))
        ));
    }

    @Test
    public void getting_account_by_id_should_return_it_if_it_exists() throws AccountNotFoundException {
        when(accountDao.getAccountById(1)).thenReturn(Optional.of(new DbAccount(1, 10)));

        Account account = accountController.getAccountById(1);

        assertThat(account, samePropertyValuesAs(expectedAccount(1, 10)));
    }

    @Test
    public void getting_account_by_id_should_throw_exception_if_it_does_not_exist() throws AccountNotFoundException {
        when(accountDao.getAccountById(1)).thenReturn(Optional.empty());

        thrown.expect(AccountNotFoundException.class);
        thrown.expect(hasProperty("accountId", equalTo(1)));

        accountController.getAccountById(1);
    }

    private NewAccountRequest newAccountRequestWithBalance(int balance) {
        NewAccountRequest request = new NewAccountRequest();
        request.setBalance(balance);
        return request;
    }

    private Account expectedAccount(int accountId, int balance) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setBalance(balance);
        return account;
    }

}