package nayan92.moneytransfer.db;

import com.google.inject.PrivateModule;
import nayan92.moneytransfer.db.dao.AccountDao;

import javax.inject.Singleton;

public class DbModule extends PrivateModule {
    @Override
    protected void configure() {
        bind(JdbiProvider.class).in(Singleton.class);
        bind(AccountDao.class).in(Singleton.class);
        expose(AccountDao.class);
        expose(JdbiProvider.class);
    }
}
