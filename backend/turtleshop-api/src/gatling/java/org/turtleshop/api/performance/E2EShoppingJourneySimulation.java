package org.turtleshop.api.performance;

import io.github.cdimascio.dotenv.Dotenv;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;

import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class E2EShoppingJourneySimulation extends Simulation {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory("../../")
            .ignoreIfMissing()
            .load();

    private static final String adminEmail = dotenv.get("ADMIN_EMAIL", "default-admin@turtleshop.com");
    private static final String adminPassword = dotenv.get("ADMIN_PASSWORD", "default-password");

    /*
     * This simulation uses product 4 only.
     * This avoids product 9, which your TransactionFlowSimulation already uses.
     */
    private static final int E2E_TEST_PRODUCT_ID = 4;
    private static final int E2E_TEST_STOCK = 1_000_000;

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder resetE2ETestData = scenario("Reset E2E Shopping Journey Test Data")

            .exec(http("Admin Login For E2E Test Setup")
                    .post("/api/auth/login")
                    .body(StringBody(String.format(
                            "{\"email\":\"%s\",\"password\":\"%s\"}",
                            adminEmail,
                            adminPassword
                    )))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("adminJwtToken")))
            .exitHereIfFailed()

            .exec(http("Reset E2E Product Inventory")
                    .put("/api/inventory/product/" + E2E_TEST_PRODUCT_ID)
                    .header("Authorization", "Bearer #{adminJwtToken}")
                    .body(StringBody("{"
                            + "\"quantityAvailable\":" + E2E_TEST_STOCK + ","
                            + "\"quantityReserved\":0"
                            + "}"))
                    .check(status().is(204)))
            .exitHereIfFailed();

    ScenarioBuilder scn = scenario("Full E2E: Register, Search, Cart, Checkout, Review")

            .exec(session -> {
                String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

                return session
                        .set("dynamicEmail", "shopper_" + uniqueId + "@turtleshop.com")
                        .set("dynamicPassword", "SecurePass123!")
                        .set("targetProductId", String.valueOf(E2E_TEST_PRODUCT_ID));
            })

            .exec(http("Customer Registration")
                    .post("/api/auth/register")
                    .body(StringBody("{"
                            + "\"email\":\"#{dynamicEmail}\","
                            + "\"password\":\"#{dynamicPassword}\","
                            + "\"firstName\":\"Donatello\","
                            + "\"lastName\":\"Splinterson\","
                            + "\"phone\":\"+17166383774\""
                            + "}"))
                    .check(status().is(200)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Customer Login")
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

            .exec(http("Search Products")
                    .get("/api/products/#{targetProductId}")
                    .check(status().is(200))
                    .check(jsonPath("$.id").exists()))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Create Cart")
                    .post("/api/cart/#{currentCustomerId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Add Product To Cart")
                    .post("/api/cart/#{currentCustomerId}/items")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{"
                            + "\"productId\":#{targetProductId},"
                            + "\"quantity\":1"
                            + "}"))
                    .check(status().in(200, 201))
                    .check(jsonPath("$.productId").exists()))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Place Order")
                    .post("/api/checkout/customer/#{currentCustomerId}/place-order")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{"
                            + "\"shippingMethod\":\"PostNL\","
                            + "\"shippingAddress\":\"123 Sewer Lair, NYC\","
                            + "\"paymentMethod\":\"Visa\""
                            + "}"))
                    .check(status().is(200))
                    .check(jsonPath("$.orderId").saveAs("newOrderId")))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Create Product Review")
                    .post("/api/products/#{targetProductId}/reviews")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{"
                            + "\"customerId\":\"#{currentCustomerId}\","
                            + "\"rating\":5,"
                            + "\"comment\":\"Gatling automated E2E review test.\""
                            + "}"))
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("createdReviewId"))
                    .check(jsonPath("$.productId").exists())
                    .check(jsonPath("$.customerId").exists()))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Get Product Reviews")
                    .get("/api/products/#{targetProductId}/reviews")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)));

    {
        setUp(
                resetE2ETestData.injectOpen(atOnceUsers(1))
                        .andThen(
                                scn.injectOpen(
                                        atOnceUsers(15)
                                )
                        )
        ).protocols(httpProtocol);
    }
}