package nayan92.moneytransfer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import nayan92.moneytransfer.db.DbModule;
import nayan92.moneytransfer.db.JdbiProvider;
import nayan92.moneytransfer.db.dao.AccountDao;
import nayan92.moneytransfer.db.data.BulkUpdate;
import nayan92.moneytransfer.db.entity.DbAccount;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AccountDaoIntegrationTest {

    private JdbiProvider jdbiProvider;

    private AccountDao accountDao;

    @Before
    public void setUp() throws IOException {
        Injector injector = Guice.createInjector(new DbModule());
        accountDao = injector.getInstance(AccountDao.class);
        jdbiProvider = injector.getInstance(JdbiProvider.class);

        jdbiProvider.initialise();
    }

    @After
    public void tearDown() {
        jdbiProvider.get().useHandle(handle -> {
            handle.execute("drop all objects");
        });;
    }

    @Test
    public void creating_account_stores_a_new_entry_in_the_database() {
        accountDao.createAccount(50);

        jdbiProvider.get().useHandle(handle -> {
            Map<String, Object> account = handle.createQuery("select * from account").mapToMap().one();

            assertThat(account, hasEntry("account_id", 1));
            assertThat(account, hasEntry("balance", 50L));
        });
    }

    @Test
    public void creating_account_returns_the_newly_created_account_id() {
        int accountId = accountDao.createAccount(50);

        assertThat(accountId, equalTo(1));
    }

    @Test
    public void getting_all_accounts_returns_all_accounts_in_the_database() {
        jdbiProvider.get().useHandle(handle -> {
            handle.execute("insert into account (account_id, balance) values (12345, 100)");
            handle.execute("insert into account (account_id, balance) values (54321, 200)");
        });

        List<DbAccount> allAccounts = accountDao.getAllAccounts();

        assertThat(allAccounts, contains(
            samePropertyValuesAs(new DbAccount(12345, 100)),
            samePropertyValuesAs(new DbAccount(54321, 200))
        ));
    }

    @Test
    public void getting_account_by_id_returns_account_if_it_exists() {
        jdbiProvider.get().useHandle(handle -> {
            handle.execute("insert into account (account_id, balance) values (12345, 100)");
        });

        Optional<DbAccount> maybeAccount = accountDao.getAccountById(12345);

        assertThat(maybeAccount.isPresent(), is(true));
        assertThat(maybeAccount.get(), samePropertyValuesAs(new DbAccount(12345, 100)));
    }

    @Test
    public void getting_account_by_id_returns_nothing_if_it_does_not_exists() {
        Optional<DbAccount> maybeAccount = accountDao.getAccountById(12345);

        assertThat(maybeAccount.isPresent(), is(false));
    }

    @Test
    public void bulk_updated_balance_updates_all_accounts_specified() {
        jdbiProvider.get().useHandle(handle -> {
            handle.execute("insert into account (account_id, balance) values (12345, 100)");
            handle.execute("insert into account (account_id, balance) values (54321, 200)");
        });

        accountDao.bulkUpdateBalance(asList(
            new BulkUpdate(12345, 200),
            new BulkUpdate(54321, 300)
        ));

        jdbiProvider.get().useHandle(handle -> {
            Map<String, Object> account1 = handle.createQuery("select * from account where account_id = 12345").mapToMap().one();
            Map<String, Object> account2 = handle.createQuery("select * from account where account_id = 54321").mapToMap().one();

            assertThat(account1, hasEntry("balance", 200L));
            assertThat(account2, hasEntry("balance", 300L));
        });
    }

}
