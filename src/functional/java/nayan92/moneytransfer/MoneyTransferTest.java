package nayan92.moneytransfer;

import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

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

    private int createAccountWithBalance(int balance) {
        return given()
            .body("{ \"balance\": " + balance + " }")
        .when()
            .post("/accounts")
        .then().extract()
            .path("accountId");
    }

}
