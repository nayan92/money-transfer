package nayan92.moneytransfer.restapi;

import com.google.gson.Gson;
import nayan92.moneytransfer.controller.AccountController;
import nayan92.moneytransfer.controller.TransactionController;
import nayan92.moneytransfer.data.request.NewAccountRequest;
import nayan92.moneytransfer.data.request.TransferRequest;
import nayan92.moneytransfer.data.response.Account;

import java.util.List;

import static spark.Spark.*;

public class Router {

    private final AccountController accountController;
    private final TransactionController transactionController;

    private final Gson gson = new Gson(); // TODO pull out into utils

    public Router(AccountController accountController, TransactionController transactionController) {
        this.accountController = accountController;
        this.transactionController = transactionController;
    }

    public void start() {
        path("/accounts", () -> {
            get("", (request, response) -> {
                response.type("application/json");
                List<Account> allAccounts = accountController.getAllAccounts();
                return gson.toJson(allAccounts);
            });
            post("", (request, response) -> {
                response.type("application/json");
                NewAccountRequest newAccountRequest = gson.fromJson(request.body(), NewAccountRequest.class);
                Account newAccount = accountController.createAccount(newAccountRequest);
                return gson.toJson(newAccount);
            });
            path("/:id", () -> {
                get("", (request, response) -> {
                    response.type("application/json");
                    int accountId = Integer.parseInt(request.params("id"));
                    Account account = accountController.getAccountById(accountId);
                    return gson.toJson(account);
                });
            });
        });
        post("/transactions", (request, response) -> {
            response.type("application/json");
            TransferRequest transferRequest = gson.fromJson(request.body(), TransferRequest.class);
            List<Account> updatedAccounts = transactionController.transfer(transferRequest);
            return gson.toJson(updatedAccounts);
        });
    }

    public void kill() {
        stop();
    }

}
