package nayan92.moneytransfer.controller;

import com.google.inject.PrivateModule;
import nayan92.moneytransfer.controller.mapper.AccountMapper;

import javax.inject.Singleton;

public class ControllerModule extends PrivateModule {
    @Override
    protected void configure() {
        bind(AccountMapper.class).in(Singleton.class);
        bind(AccountController.class).in(Singleton.class);
        bind(TransactionController.class).in(Singleton.class);
        expose(AccountController.class);
        expose(TransactionController.class);
    }
}
