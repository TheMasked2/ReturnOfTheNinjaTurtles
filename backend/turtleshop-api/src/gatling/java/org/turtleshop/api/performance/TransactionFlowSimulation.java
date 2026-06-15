package org.turtleshop.api.performance;

import io.github.cdimascio.dotenv.Dotenv;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;

import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class TransactionFlowSimulation extends Simulation {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory("../../")
            .ignoreIfMissing()
            .load();

    private static final String adminEmail = dotenv.get("ADMIN_EMAIL", "default-admin@turtleshop.com");
    private static final String adminPassword = dotenv.get("ADMIN_PASSWORD", "default-password");

    private static final int TRANSACTION_TEST_PRODUCT_ID = 9;
    private static final int TRANSACTION_TEST_STOCK = 10000;

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder resetTransactionTestData = scenario("Reset Transaction Test Data")
            .exec(http("Admin Login For Transaction Test Setup")
                    .post("/api/auth/login")
                    .body(StringBody(String.format(
                            "{\"email\":\"%s\",\"password\":\"%s\"}",
                            adminEmail,
                            adminPassword
                    )))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("adminJwtToken")))
            .exitHereIfFailed()

            .exec(http("Reset Transaction Test Product Inventory")
                    .put("/api/inventory/product/" + TRANSACTION_TEST_PRODUCT_ID)
                    .header("Authorization", "Bearer #{adminJwtToken}")
                    .body(StringBody("{"
                            + "\"quantityAvailable\":" + TRANSACTION_TEST_STOCK + ","
                            + "\"quantityReserved\":0"
                            + "}"))
                    .check(status().is(204)))
            .exitHereIfFailed();

    ScenarioBuilder scn = scenario("Transaction Payment Confirmation Flow")
            .exec(session -> {
                String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

                return session
                        .set("dynamicEmail", "transaction_user_" + uniqueId + "@turtleshop.com")
                        .set("dynamicPassword", "SecurePass123!");
            })

            .exec(http("Register Transaction Customer")
                    .post("/api/auth/register")
                    .body(StringBody("{"
                            + "\"email\":\"#{dynamicEmail}\","
                            + "\"password\":\"#{dynamicPassword}\","
                            + "\"firstName\":\"April\","
                            + "\"lastName\":\"ONeil\","
                            + "\"phone\":\"+17165550123\""
                            + "}"))
                    .check(status().is(200)))
            .pause(1)

            .exec(http("Login Transaction Customer")
                    .post("/api/auth/login")
                    .body(StringBody("{"
                            + "\"email\":\"#{dynamicEmail}\","
                            + "\"password\":\"#{dynamicPassword}\""
                            + "}"))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("jwtToken"))
                    .check(jsonPath("$.customer.id").saveAs("currentCustomerId")))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Create Cart For Transaction Flow")
                    .post("/api/cart/#{currentCustomerId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .pause(1)

            .exec(http("Add Product For Transaction Flow")
                    .post("/api/cart/#{currentCustomerId}/items")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{"
                            + "\"productId\":" + TRANSACTION_TEST_PRODUCT_ID + ","
                            + "\"quantity\":1"
                            + "}"))
                    .check(status().in(200, 201)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Place Order And Create Pending Transaction")
                    .post("/api/checkout/customer/#{currentCustomerId}/place-order")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{"
                            + "\"shippingMethod\":\"PostNL\","
                            + "\"shippingAddress\":\"123 Transaction Street, Amsterdam\","
                            + "\"paymentMethod\":\"Visa\""
                            + "}"))
                    .check(status().is(200))
                    .check(jsonPath("$.orderId").saveAs("orderId"))
                    .check(jsonPath("$.transactionId").saveAs("transactionId"))
                    .check(jsonPath("$.transactionStatus").is("PENDING"))
                    .check(jsonPath("$.totalAmount").saveAs("orderTotal")))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Customer Reads Transactions For Own Order")
                    .get("/api/transactions/order/#{orderId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200))
                    .check(jsonPath("$[0].status").is("PENDING")))
            .pause(1)

            .exec(http("Admin Login For Payment Confirmation")
                    .post("/api/auth/login")
                    .body(StringBody(String.format(
                            "{\"email\":\"%s\",\"password\":\"%s\"}",
                            adminEmail,
                            adminPassword
                    )))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("adminJwtToken")))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Admin Confirms Transaction Payment")
                    .post("/api/transactions/#{transactionId}/confirm-payment?orderId=#{orderId}")
                    .header("Authorization", "Bearer #{adminJwtToken}")
                    .check(status().is(204)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Admin Verifies Transaction Is Successful")
                    .get("/api/transactions/#{transactionId}")
                    .header("Authorization", "Bearer #{adminJwtToken}")
                    .check(status().is(200))
                    .check(jsonPath("$.status").is("SUCCESS")))
            .pause(1)

            .exec(http("Customer Verifies Order Is Confirmed")
                    .get("/api/orders/#{orderId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200))
                    .check(jsonPath("$.status").is("CONFIRMED")));

    {
        setUp(
                resetTransactionTestData.injectOpen(atOnceUsers(1))
                        .andThen(
                                scn.injectOpen(
                                        rampUsers(50).during(20),
                                        constantUsersPerSec(15).during(30)
                                )
                        )
        ).protocols(httpProtocol);
    }
}