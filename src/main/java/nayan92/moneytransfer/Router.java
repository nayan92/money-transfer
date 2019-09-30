package nayan92.moneytransfer;

import static spark.Spark.*;

public class Router {

    public void start() {
        path("/accounts", () -> {
            get("", (request, response) -> {
                return "Get all accounts";
            });
            post("", (request, response) -> {
                return "Create an account";
            });
            path("/:id", () -> {
                get("", (request, response) -> {
                    return "Get account by id";
                });
                post("/transfer", (request, response) -> {
                    return "Transfer money to another account";
                });
            });
        });
        get("ping", (request, response) -> "pong");
    }

}
