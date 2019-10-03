package nayan92.moneytransfer;

import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MoneyTransferTest {

    private Server server;

    @BeforeClass
    public static void configure() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 4567;
    }

    @Before
    public void setUp() {
        server = new Server();
        server.start();
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void should_transfer_money_between_two_accounts() {
        int fromAccountId = createAccountWithBalance(100);
        int toAccountId = createAccountWithBalance(200);
        int amountToTransfer = 50;

        given()
            .body(String.format("{ \"fromAccountId\": %d, \"toAccountId\": %d, \"amount\": %d }", fromAccountId, toAccountId, amountToTransfer))
        .when()
            .post("/transactions")
        .then()
            .body("[0].accountId", equalTo(fromAccountId))
            .body("[0].balance", equalTo(50))
            .body("[1].accountId", equalTo(toAccountId))
            .body("[1].balance", equalTo(250));
    }

    @Test
    public void multiple_transfers_should_happen_independently_of_each_other() throws InterruptedException {
        int fromAccountId = createAccountWithBalance(500);
        int toAccountId = createAccountWithBalance(500);
        int amountToTransfer = 5;
        ExecutorService executor = Executors.newFixedThreadPool(50);

        performMultipleConcurrentTransfers(executor, 50, fromAccountId, toAccountId, amountToTransfer);
        executor.shutdown();
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);

        assertThat(getBalanceForAccountId(fromAccountId), equalTo(250));
        assertThat(getBalanceForAccountId(toAccountId), equalTo(750));
    }

    @Test
    public void multiple_transfers_between_accounts_should_not_block() throws InterruptedException {
        int account1 = createAccountWithBalance(500);
        int account2 = createAccountWithBalance(500);
        int amountToTransfer = 5;
        ExecutorService executor = Executors.newFixedThreadPool(50);

        performMultipleConcurrentTransfers(executor, 25, account1, account2, amountToTransfer);
        performMultipleConcurrentTransfers(executor, 25, account2, account1, amountToTransfer);
        executor.shutdown();

        boolean didNotTimeout = executor.awaitTermination(5000, TimeUnit.MILLISECONDS);

        assertThat(didNotTimeout, is(true));
        assertThat(getBalanceForAccountId(account1), equalTo(500));
        assertThat(getBalanceForAccountId(account2), equalTo(500));
    }

    private void performMultipleConcurrentTransfers(ExecutorService executor, int numberOfRequests, int fromAccountId, int toAccountId, int amount) {
        for (int i = 0; i < numberOfRequests; i++) {
            executor.execute(() -> {
                given()
                    .body(String.format("{ \"fromAccountId\": %d, \"toAccountId\": %d, \"amount\": %d }", fromAccountId, toAccountId, amount))
                .when()
                    .post("/transactions");
            });
        }
    }

    private int createAccountWithBalance(int balance) {
        return given()
            .body("{ \"balance\": " + balance + " }")
        .when()
            .post("/accounts")
        .then().extract()
            .path("accountId");
    }

    private int getBalanceForAccountId(int accountId) {
        return when()
            .get("/accounts/" + accountId)
        .then().extract()
            .path("balance");
    }

}
