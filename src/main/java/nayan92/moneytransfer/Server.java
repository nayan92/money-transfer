package nayan92.moneytransfer;


import com.google.inject.Guice;
import com.google.inject.Injector;
import nayan92.moneytransfer.controller.ControllerModule;
import nayan92.moneytransfer.db.DbModule;
import nayan92.moneytransfer.db.JdbiProvider;
import nayan92.moneytransfer.restapi.ApiModule;
import nayan92.moneytransfer.restapi.Router;

public class Server {

    private Router router;

    public void start() {
        Injector injector = Guice.createInjector(new ControllerModule(), new ApiModule(), new DbModule());
        JdbiProvider dbProvider = injector.getInstance(JdbiProvider.class);
        router = injector.getInstance(Router.class);

        dbProvider.initialise();
        router.start();
    }

    public void stop() {
        router.kill();
    }

}
