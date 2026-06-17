package org.turtleshop.api.performance;

import io.github.cdimascio.dotenv.Dotenv;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;

import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class GraphRecommendationSimulation extends Simulation {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory("../../")
            .ignoreIfMissing()
            .load();

    private static final String adminEmail = dotenv.get("ADMIN_EMAIL", "default-admin@turtleshop.com");
    private static final String adminPassword = dotenv.get("ADMIN_PASSWORD", "default-password");

    /*
     * This simulation uses product 5 and product 6 only.
     *
     * Target customer buys product 5.
     * Similar customer buys product 5 and product 6.
     * Therefore product 6 should be recommended to the target customer.
     */
    private static final int SHARED_PRODUCT_ID = 5;
    private static final int RECOMMENDED_PRODUCT_ID = 6;
    private static final int GRAPH_TEST_STOCK = 1_000_000;

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder resetGraphRecommendationTestData = scenario("Reset Graph Recommendation Test Data")

            .exec(http("Admin Login For Graph Recommendation Setup")
                    .post("/api/auth/login")
                    .body(StringBody(String.format(
                            "{\"email\":\"%s\",\"password\":\"%s\"}",
                            adminEmail,
                            adminPassword
                    )))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("adminJwtToken")))
            .exitHereIfFailed()

            .exec(http("Reset Shared Product Inventory")
                    .put("/api/inventory/product/" + SHARED_PRODUCT_ID)
                    .header("Authorization", "Bearer #{adminJwtToken}")
                    .body(StringBody("{"
                            + "\"quantityAvailable\":" + GRAPH_TEST_STOCK + ","
                            + "\"quantityReserved\":0"
                            + "}"))
                    .check(status().is(204)))
            .exitHereIfFailed()

            .exec(http("Reset Recommended Product Inventory")
                    .put("/api/inventory/product/" + RECOMMENDED_PRODUCT_ID)
                    .header("Authorization", "Bearer #{adminJwtToken}")
                    .body(StringBody("{"
                            + "\"quantityAvailable\":" + GRAPH_TEST_STOCK + ","
                            + "\"quantityReserved\":0"
                            + "}"))
                    .check(status().is(204)))
            .exitHereIfFailed();

    ScenarioBuilder scn = scenario("Graph DB Recommendation Full Flow")

            .exec(session -> {
                String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

                return session
                        .set("targetEmail", "graph_target_" + uniqueId + "@turtleshop.com")
                        .set("similarEmail", "graph_similar_" + uniqueId + "@turtleshop.com")
                        .set("password", "SecurePass123!");
            })

            .exec(http("Register Target Customer")
                    .post("/api/auth/register")
                    .body(StringBody("{"
                            + "\"email\":\"#{targetEmail}\","
                            + "\"password\":\"#{password}\","
                            + "\"firstName\":\"Leonardo\","
                            + "\"lastName\":\"GraphTarget\","
                            + "\"phone\":\"+17165550001\""
                            + "}"))
                    .check(status().is(200)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Login Target Customer")
                    .post("/api/auth/login")
                    .body(StringBody("{"
                            + "\"email\":\"#{targetEmail}\","
                            + "\"password\":\"#{password}\""
                            + "}"))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("targetJwtToken"))
                    .check(jsonPath("$.customer.id").saveAs("targetCustomerId")))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Create Cart For Target Customer")
                    .post("/api/cart/#{targetCustomerId}")
                    .header("Authorization", "Bearer #{targetJwtToken}")
                    .check(status().is(200)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Target Customer Adds Shared Product")
                    .post("/api/cart/#{targetCustomerId}/items")
                    .header("Authorization", "Bearer #{targetJwtToken}")
                    .body(StringBody("{"
                            + "\"productId\":" + SHARED_PRODUCT_ID + ","
                            + "\"quantity\":1"
                            + "}"))
                    .check(status().in(200, 201)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Target Customer Places Order")
                    .post("/api/checkout/customer/#{targetCustomerId}/place-order")
                    .header("Authorization", "Bearer #{targetJwtToken}")
                    .body(StringBody("{"
                            + "\"shippingMethod\":\"PostNL\","
                            + "\"shippingAddress\":\"123 Graph Target Street\","
                            + "\"paymentMethod\":\"Visa\""
                            + "}"))
                    .check(status().is(200))
                    .check(jsonPath("$.orderId").saveAs("targetOrderId"))
                    .check(jsonPath("$.transactionId").saveAs("targetTransactionId")))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Register Similar Customer")
                    .post("/api/auth/register")
                    .body(StringBody("{"
                            + "\"email\":\"#{similarEmail}\","
                            + "\"password\":\"#{password}\","
                            + "\"firstName\":\"Raphael\","
                            + "\"lastName\":\"GraphSimilar\","
                            + "\"phone\":\"+17165550002\""
                            + "}"))
                    .check(status().is(200)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Login Similar Customer")
                    .post("/api/auth/login")
                    .body(StringBody("{"
                            + "\"email\":\"#{similarEmail}\","
                            + "\"password\":\"#{password}\""
                            + "}"))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("similarJwtToken"))
                    .check(jsonPath("$.customer.id").saveAs("similarCustomerId")))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Create Cart For Similar Customer")
                    .post("/api/cart/#{similarCustomerId}")
                    .header("Authorization", "Bearer #{similarJwtToken}")
                    .check(status().is(200)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Similar Customer Adds Shared Product")
                    .post("/api/cart/#{similarCustomerId}/items")
                    .header("Authorization", "Bearer #{similarJwtToken}")
                    .body(StringBody("{"
                            + "\"productId\":" + SHARED_PRODUCT_ID + ","
                            + "\"quantity\":1"
                            + "}"))
                    .check(status().in(200, 201)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Similar Customer Adds Recommended Product")
                    .post("/api/cart/#{similarCustomerId}/items")
                    .header("Authorization", "Bearer #{similarJwtToken}")
                    .body(StringBody("{"
                            + "\"productId\":" + RECOMMENDED_PRODUCT_ID + ","
                            + "\"quantity\":1"
                            + "}"))
                    .check(status().in(200, 201)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Similar Customer Places Order")
                    .post("/api/checkout/customer/#{similarCustomerId}/place-order")
                    .header("Authorization", "Bearer #{similarJwtToken}")
                    .body(StringBody("{"
                            + "\"shippingMethod\":\"PostNL\","
                            + "\"shippingAddress\":\"456 Graph Similar Street\","
                            + "\"paymentMethod\":\"Visa\""
                            + "}"))
                    .check(status().is(200))
                    .check(jsonPath("$.orderId").saveAs("similarOrderId"))
                    .check(jsonPath("$.transactionId").saveAs("similarTransactionId")))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Admin Login For Graph Payment Confirmation")
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

            .exec(http("Admin Confirms Target Payment")
                    .post("/api/transactions/#{targetTransactionId}/confirm-payment?orderId=#{targetOrderId}")
                    .header("Authorization", "Bearer #{adminJwtToken}")
                    .check(status().is(204)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Admin Confirms Similar Payment")
                    .post("/api/transactions/#{similarTransactionId}/confirm-payment?orderId=#{similarOrderId}")
                    .header("Authorization", "Bearer #{adminJwtToken}")
                    .check(status().is(204)))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Similar Customer Reviews Recommended Product")
                    .post("/api/products/" + RECOMMENDED_PRODUCT_ID + "/reviews")
                    .header("Authorization", "Bearer #{similarJwtToken}")
                    .body(StringBody("{"
                            + "\"customerId\":\"#{similarCustomerId}\","
                            + "\"rating\":5,"
                            + "\"comment\":\"Graph recommendation Gatling review.\""
                            + "}"))
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("createdReviewId")))
            .exitHereIfFailed()
            .pause(1)

            .exec(http("Get Seasonal Graph Recommendations")
                    .get("/api/recommendations/seasonal?customerId=#{targetCustomerId}&limit=5")
                    .header("Authorization", "Bearer #{targetJwtToken}")
                    .check(status().is(200))
                    .check(jsonPath("$[?(@.productId == " + RECOMMENDED_PRODUCT_ID + ")].productId").exists()));

    {
        setUp(
                resetGraphRecommendationTestData.injectOpen(atOnceUsers(1))
                        .andThen(
                                scn.injectOpen(
                                        rampUsers(50).during(20),
                                        constantUsersPerSec(15).during(30)
                                )
                        )
        ).protocols(httpProtocol);
    }
}