package nayan92.moneytransfer;


import nayan92.moneytransfer.controller.AccountController;
import nayan92.moneytransfer.controller.TransactionController;
import nayan92.moneytransfer.controller.mapper.AccountMapper;
import nayan92.moneytransfer.db.JdbiProvider;
import nayan92.moneytransfer.db.dao.AccountDao;
import nayan92.moneytransfer.restapi.Router;

public class Server {

    private Router router;

    public void start() {
        JdbiProvider dbProvider = new JdbiProvider();
        AccountDao accountDAO = new AccountDao(dbProvider);
        AccountMapper accountMapper = new AccountMapper();

        AccountController accountController = new AccountController(accountDAO, accountMapper);
        TransactionController transactionController = new TransactionController(accountDAO, accountMapper);
        router = new Router(accountController, transactionController);

        dbProvider.initialise();
        router.start();
    }

    public void stop() {
        router.kill();
    }

}
