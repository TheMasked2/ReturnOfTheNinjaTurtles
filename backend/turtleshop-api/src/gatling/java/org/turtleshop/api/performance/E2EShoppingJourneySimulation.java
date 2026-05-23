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
                    .set("dynamicEmail", "reviewer_" + uniqueId + "@turtleshop.com")
                    .set("dynamicPassword", "SecurePass123!");
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
        .pause(1)

        // Find Product
        .exec(http("Search Products")
                .get("/api/products?search=Turtle")
                .check(status().is(200))
                .check(jsonPath("$[*].id").findRandom().saveAs("targetProductId")))
        .pause(1)

        // Create Cart
        .exec(http("Create Cart")
                .post("/api/cart/#{currentCustomerId}")
                .header("Authorization", "Bearer #{jwtToken}")
                .check(status().is(200)))
        .pause(1)

        // Add Product To Cart
        .exec(http("Add Product To Cart")
                .post("/api/cart/#{currentCustomerId}/items")
                .header("Authorization", "Bearer #{jwtToken}")
                .body(StringBody("{"
                        + "\"productId\": #{targetProductId},"
                        + "\"quantity\": 1"
                        + "}"))
                .check(status().in(200, 201)))
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
        .pause(2)

        // Create Review
        // Uses ProductReviewController POST /api/products/{productId}/reviews
        // Uses ReviewRequest DTO
        .exec(http("Create Product Review")
                .post("/api/products/#{targetProductId}/reviews")
                .header("Authorization", "Bearer #{jwtToken}")
                .body(StringBody("{"
                        + "\"customerId\":\"#{currentCustomerId}\","
                        + "\"rating\":5,"
                        + "\"comment\":\"Gatling automated review test.\""
                        + "}"))
                .check(status().is(201))
                .check(jsonPath("$.id").saveAs("createdReviewId"))
                .check(jsonPath("$.productId").is("#{targetProductId}")))
        .pause(1)

        // Get Product Reviews
        .exec(http("Get Product Reviews")
                .get("/api/products/#{targetProductId}/reviews")
                .header("Authorization", "Bearer #{jwtToken}")
                .check(status().is(200)));
    {
        setUp(
                scn.injectOpen(rampUsers(15).during(10))
        ).protocols(httpProtocol);
    }
}