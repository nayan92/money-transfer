package nayan92.moneytransfer.restapi;

import com.google.inject.PrivateModule;
import nayan92.moneytransfer.controller.AccountController;
import nayan92.moneytransfer.controller.TransactionController;

import javax.inject.Singleton;

public class ApiModule extends PrivateModule {
    @Override
    protected void configure() {
        requireBinding(AccountController.class);
        requireBinding(TransactionController.class);
        bind(Router.class).in(Singleton.class);
        expose(Router.class);
    }
}
