package nayan92.moneytransfer;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.restassured.RestAssured;
import nayan92.moneytransfer.controller.AccountController;
import nayan92.moneytransfer.controller.TransactionController;
import nayan92.moneytransfer.data.exception.AccountNotFoundException;
import nayan92.moneytransfer.data.exception.InsufficientFundsException;
import nayan92.moneytransfer.data.exception.SameAccountTransferException;
import nayan92.moneytransfer.data.request.NewAccountRequest;
import nayan92.moneytransfer.data.request.TransferRequest;
import nayan92.moneytransfer.data.response.Account;
import nayan92.moneytransfer.data.response.Error;
import nayan92.moneytransfer.restapi.ApiModule;
import nayan92.moneytransfer.restapi.Router;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ApiIntegrationTest {

    @Mock AccountController accountController;
    @Mock TransactionController transactionController;

    private Gson gson = new Gson();

    private Router router;

    @Before
    public void setUp() {
        Injector injector = Guice.createInjector(new ApiModule(), new TestModule());
        router = injector.getInstance(Router.class);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 4567;

        router.start();
    }

    @After
    public void tearDown() {
        router.kill();
    }

    @Test
    public void getting_all_accounts_returns_the_correct_response() {
        List<Account> allAccounts = asList(account(1, 10), account(2, 20));
        when(accountController.getAllAccounts()).thenReturn(allAccounts);

        RestAssured.when()
            .get("/accounts")
        .then()
            .statusCode(equalTo(200))
            .contentType(equalTo("application/json"))
            .body(equalTo(gson.toJson(allAccounts)));
    }

    @Test
    public void creating_an_account_returns_the_correct_response() throws AccountNotFoundException {
        Account newAccount = account(10, 500);
        when(accountController.createAccount(argThat((req) -> matches(req, newAccountRequest(500)))))
                .thenReturn(newAccount);

        given()
            .body("{ \"balance\": 500 }")
        .when()
            .post("/accounts")
        .then()
            .statusCode(equalTo(201))
            .contentType(equalTo("application/json"))
            .body(equalTo(gson.toJson(newAccount)));
    }

    @Test
    public void getting_account_by_id_returns_the_correct_response() throws AccountNotFoundException {
        Account requestedAccount = account(10, 500);
        when(accountController.getAccountById(10)).thenReturn(requestedAccount);

        RestAssured.when()
            .get("/accounts/10")
        .then()
            .statusCode(equalTo(200))
            .contentType(equalTo("application/json"))
            .body(equalTo(gson.toJson(requestedAccount)));
    }

    @Test
    public void performing_a_transaction_returns_the_correct_response() throws AccountNotFoundException, InsufficientFundsException, SameAccountTransferException {
        List<Account> updatedAccounts = asList(account(1, 10), account(2, 20));
        when(transactionController.transfer(argThat((req) -> matches(req, transferRequest(1, 2, 10)))))
                .thenReturn(updatedAccounts);

        given()
            .body("{ \"fromAccountId\": 1, \"toAccountId\": 2, \"amount\": 10 }")
        .when()
            .post("/transactions")
        .then()
            .statusCode(equalTo(200))
            .contentType(equalTo("application/json"))
            .body(equalTo(gson.toJson(updatedAccounts)));
    }

    @Test
    public void getting_account_by_id_which_throws_account_not_found_exception_returns_correct_error() throws AccountNotFoundException {
        when(accountController.getAccountById(any(Integer.class))).thenThrow(new AccountNotFoundException(10));

        RestAssured.when()
            .get("/accounts/10")
        .then()
            .statusCode(equalTo(404))
            .contentType(equalTo("application/json"))
            .body(equalTo(gson.toJson(error("An account with id (10) does not exist"))));
    }

    @Test
    public void performing_a_transaction_which_throws_insufficient_funds_exception_returns_correct_error() throws AccountNotFoundException, InsufficientFundsException, SameAccountTransferException {
        when(transactionController.transfer(any(TransferRequest.class))).thenThrow(new InsufficientFundsException());

        given()
            .body("{ \"fromAccountId\": 1, \"toAccountId\": 2, \"amount\": 10 }")
        .when()
            .post("/transactions")
        .then()
            .statusCode(equalTo(409))
            .contentType(equalTo("application/json"))
            .body(equalTo(gson.toJson(error("The account does not have enough funds to complete the transfer"))));
    }

    @Test
    public void performing_a_transaction_which_throws_same_account_transfer_exception_returns_correct_error() throws AccountNotFoundException, InsufficientFundsException, SameAccountTransferException {
        when(transactionController.transfer(any(TransferRequest.class))).thenThrow(new SameAccountTransferException());

        given()
            .body("{ \"fromAccountId\": 1, \"toAccountId\": 2, \"amount\": 10 }")
        .when()
            .post("/transactions")
        .then()
            .statusCode(equalTo(409))
            .contentType(equalTo("application/json"))
            .body(equalTo(gson.toJson(error("The transfer must be made between two different accounts"))));
    }

    private boolean matches(Object expected, NewAccountRequest actual) {
        return ((NewAccountRequest) expected).getBalance() == actual.getBalance();
    }

    private boolean matches(Object expected, TransferRequest actual) {
        TransferRequest expectedReq = (TransferRequest) expected;
        return expectedReq.getFromAccountId() == actual.getFromAccountId()
                && expectedReq.getToAccountId() == actual.getToAccountId()
                && expectedReq.getAmount() == actual.getAmount();
    }

    private NewAccountRequest newAccountRequest(int balance) {
        NewAccountRequest request = new NewAccountRequest();
        request.setBalance(balance);
        return request;
    }

    private TransferRequest transferRequest(int fromAccountId, int toAccountId, int amount) {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(fromAccountId);
        request.setToAccountId(toAccountId);
        request.setAmount(amount);
        return request;
    }

    private Account account(int accountId, int balance) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setBalance(balance);
        return account;
    }

    private Error error(String message) {
        Error error = new Error();
        error.setReason(message);
        return error;
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(AccountController.class).toInstance(accountController);
            bind(TransactionController.class).toInstance(transactionController);
        }
    }

}
