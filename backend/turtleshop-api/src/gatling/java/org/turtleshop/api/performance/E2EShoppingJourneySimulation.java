package org.turtleshop.api.performance;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.util.UUID;

public class E2EShoppingJourneySimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Full E2E: Register, Search, Cart, Checkout, Review")

            .exec(session -> {
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);

                return session
                        .set("dynamicEmail", "shopper_" + uniqueId + "@turtleshop.com")
                        .set("dynamicPassword", "SecurePass123!")
                        .set("targetProductId", "9");
            })

            // Register
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

            // Login
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

            // Get Product
            .exec(http("Search Products")
                    .get("/api/products/#{targetProductId}")
                    .check(status().is(200))
                    .check(jsonPath("$.id").exists()))
            .exitHereIfFailed()
            .pause(1)

            // Create Cart
            .exec(http("Create Cart")
                    .post("/api/cart/#{currentCustomerId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .exitHereIfFailed()
            .pause(1)

            // Add Product To Cart
            .exec(http("Add Product To Cart")
                    .post("/api/cart/#{currentCustomerId}/items")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{"
                            + "\"productId\": #{targetProductId},"
                            + "\"quantity\": 1"
                            + "}"))
                    .check(status().in(200, 201))
                    .check(jsonPath("$.productId").exists()))
            .exitHereIfFailed()
            .pause(1)

            // Checkout
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

            // Create Review
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

            // Get Product Reviews
            .exec(http("Get Product Reviews")
                    .get("/api/products/#{targetProductId}/reviews")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)));

    {
        setUp(
                scn.injectOpen(atOnceUsers(15))
        ).protocols(httpProtocol);
    }
}