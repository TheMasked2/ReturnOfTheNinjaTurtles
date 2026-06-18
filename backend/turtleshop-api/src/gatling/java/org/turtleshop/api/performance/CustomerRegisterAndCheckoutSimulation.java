package org.turtleshop.api.performance;

import io.github.cdimascio.dotenv.Dotenv;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;

import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class CustomerRegisterAndCheckoutSimulation extends Simulation {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory("../../")
            .ignoreIfMissing()
            .load();

    private static final String adminEmail = dotenv.get("ADMIN_EMAIL", "default-admin@turtleshop.com");
    private static final String adminPassword = dotenv.get("ADMIN_PASSWORD", "default-password");

    /*
     * This simulation uses product 3 only.
     * We reset its inventory before the performance scenario starts.
     */
    private static final int CHECKOUT_TEST_PRODUCT_ID = 3;
    private static final int CHECKOUT_TEST_STOCK = 1_000_000;

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder resetCheckoutTestData = scenario("Reset Checkout Test Data")

            .exec(http("Admin Login For Checkout Test Setup")
                    .post("/api/auth/login")
                    .body(StringBody(String.format(
                            "{\"email\":\"%s\",\"password\":\"%s\"}",
                            adminEmail,
                            adminPassword
                    )))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("adminJwtToken")))
            .exitHereIfFailed()

            .exec(http("Reset Checkout Product Inventory")
                    .put("/api/inventory/product/" + CHECKOUT_TEST_PRODUCT_ID)
                    .header("Authorization", "Bearer #{adminJwtToken}")
                    .body(StringBody("{"
                            + "\"quantityAvailable\":" + CHECKOUT_TEST_STOCK + ","
                            + "\"quantityReserved\":0"
                            + "}"))
                    .check(status().is(204)))
            .exitHereIfFailed();

    ScenarioBuilder scn = scenario("Normal User Registration & Shopping Journey")

            .exec(session -> {
                String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                String dynamicEmail = "ninja_" + uniqueId + "@turtleshop.com";

                return session
                        .set("dynamicEmail", dynamicEmail)
                        .set("dynamicPassword", "SecurePass123!");
            })

            .exec(http("Customer Registration (Sign Up)")
                    .post("/api/auth/register")
                    .body(StringBody("{"
                            + "\"email\":\"#{dynamicEmail}\","
                            + "\"password\":\"#{dynamicPassword}\","
                            + "\"firstName\":\"Michelangelo\","
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

            .exec(http("Create Empty Cart")
                    .post("/api/cart/#{currentCustomerId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Add Product to Cart")
                    .post("/api/cart/#{currentCustomerId}/items")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{"
                            + "\"productId\":" + CHECKOUT_TEST_PRODUCT_ID + ","
                            + "\"quantity\":1"
                            + "}"))
                    .check(status().in(200, 201)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Get Active Cart")
                    .get("/api/cart/#{currentCustomerId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Place Order via Checkout Controller")
                    .post("/api/checkout/customer/#{currentCustomerId}/place-order")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{"
                            + "\"shippingMethod\":\"PostNL\","
                            + "\"shippingAddress\":\"123 Sewer Lair, NYC\","
                            + "\"paymentMethod\":\"Credit Card\""
                            + "}"))
                    .check(status().is(200))
                    .check(jsonPath("$.orderId").saveAs("newOrderId"))
                    .check(jsonPath("$.totalAmount").saveAs("orderTotal")))
            .exitHereIfFailed()
            .pause(2)

            .exec(http("View Order Details")
                    .get("/api/orders/#{newOrderId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)));

    {
        setUp(
                resetCheckoutTestData.injectOpen(atOnceUsers(1))
                        .andThen(
                                scn.injectOpen(
                                        rampUsers(20).during(10)
                                )
                        )
        ).protocols(httpProtocol);
    }
}