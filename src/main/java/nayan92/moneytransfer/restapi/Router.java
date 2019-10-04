package nayan92.moneytransfer.restapi;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import nayan92.moneytransfer.controller.AccountController;
import nayan92.moneytransfer.controller.TransactionController;
import nayan92.moneytransfer.data.exception.AccountNotFoundException;
import nayan92.moneytransfer.data.exception.InsufficientFundsException;
import nayan92.moneytransfer.data.exception.SameAccountTransferException;
import nayan92.moneytransfer.data.request.NewAccountRequest;
import nayan92.moneytransfer.data.request.TransferRequest;
import nayan92.moneytransfer.data.response.Account;
import nayan92.moneytransfer.data.response.Error;
import spark.Response;

import javax.inject.Inject;
import java.util.List;

import static java.lang.Integer.parseInt;
import static spark.Spark.*;

public class Router {

    private final AccountController accountController;
    private final TransactionController transactionController;
    private final Gson gson = new Gson();

    @Inject
    public Router(AccountController accountController, TransactionController transactionController) {
        this.accountController = accountController;
        this.transactionController = transactionController;
    }

    public void start() {
        createRoutes();
        createAfters();
        createExceptionHandlers();
    }

    private void createRoutes() {
        path("/accounts", () -> {
            get("", (request, response) -> {
                List<Account> allAccounts = accountController.getAllAccounts();
                return gson.toJson(allAccounts);
            });
            post("", (request, response) -> {
                NewAccountRequest newAccountRequest = gson.fromJson(request.body(), NewAccountRequest.class);
                Account newAccount = accountController.createAccount(newAccountRequest);
                response.status(201);
                return gson.toJson(newAccount);
            });
            path("/:id", () -> {
                get("", (request, response) -> {
                    int accountId = parseInt(request.params("id"));
                    Account account = accountController.getAccountById(accountId);
                    return gson.toJson(account);
                });
            });
        });
        post("/transactions", (request, response) -> {
            TransferRequest transferRequest = gson.fromJson(request.body(), TransferRequest.class);
            List<Account> updatedAccounts = transactionController.transfer(transferRequest);
            return gson.toJson(updatedAccounts);
        });
    }

    private void createAfters() {
        after((request, response) -> response.type("application/json"));
    }

    private void createExceptionHandlers() {
        exception(AccountNotFoundException.class, (exception, request, response) -> {
            handleError(response, 404, String.format("An account with id (%s) does not exist", exception.getAccountId()));
        });
        exception(InsufficientFundsException.class, (exception, request, response) -> {
            handleError(response, 409, "The account does not have enough funds to complete the transfer");
        });
        exception(SameAccountTransferException.class, (exception, request, response) -> {
            handleError(response, 409, "The transfer must be made between two different accounts");
        });
        exception(JsonSyntaxException.class, (exception, request, response) -> {
            handleError(response, 400, "The request body is invalid");
        });
        exception(NumberFormatException.class, (exception, request, response) -> {
            handleError(response, 400, "The request path parameter must be a number");
        });
    }

    private void handleError(Response response, int status, String message) {
        Error errorResponse = new Error();
        errorResponse.setReason(message);

        response.status(status);
        response.body(gson.toJson(errorResponse));
        response.type("application/json");
    }

    public void kill() {
        stop();
        awaitStop();
    }

}
