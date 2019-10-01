package nayan92.moneytransfer;


import nayan92.moneytransfer.controller.AccountController;
import nayan92.moneytransfer.controller.TransactionController;
import nayan92.moneytransfer.controller.mapper.AccountMapper;
import nayan92.moneytransfer.db.JdbiProvider;
import nayan92.moneytransfer.db.dao.AccountDAO;
import nayan92.moneytransfer.restapi.Router;
import org.flywaydb.core.Flyway;

public class Server {

    public void start() {
        Flyway flyway = Flyway.configure()
                .dataSource("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", "", "")
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();

        JdbiProvider dbProvider = new JdbiProvider();
        AccountDAO accountDAO = new AccountDAO(dbProvider);
        AccountMapper accountMapper = new AccountMapper();

        AccountController accountController = new AccountController(accountDAO, accountMapper);
        TransactionController transactionController = new TransactionController(accountDAO, accountMapper);
        Router router = new Router(accountController, transactionController);

        dbProvider.initialise();
        router.start();
    }

}
