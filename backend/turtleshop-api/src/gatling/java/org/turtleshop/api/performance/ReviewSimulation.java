package org.turtleshop.api.performance;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.util.UUID;

public class ReviewSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Review API Smoke Test")

            .exec(session -> {
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);

                return session
                        .set("reviewEmail", "gatling_" + uniqueId + "@turtleshop.com")
                        .set("reviewPassword", "SecurePass123!");
            })

            // Register
            .exec(http("Register Review User")
                    .post("/api/auth/register")
                    .body(StringBody("{"
                            + "\"email\":\"#{reviewEmail}\","
                            + "\"password\":\"#{reviewPassword}\","
                            + "\"firstName\":\"Gatling\","
                            + "\"lastName\":\"Tester\","
                            + "\"phone\":\"+17165550000\""
                            + "}"))
                    .check(status().is(200)))
            .pause(1)

            // Login
            .exec(http("Login Review User")
                    .post("/api/auth/login")
                    .body(StringBody("{"
                            + "\"email\":\"#{reviewEmail}\","
                            + "\"password\":\"#{reviewPassword}\""
                            + "}"))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("jwtToken"))
                    .check(jsonPath("$.customer.id").saveAs("reviewCustomerId")))
            .pause(1)

            // Find Product
            .exec(http("Search Products")
                    .get("/api/products?search=Turtle")
                    .check(status().is(200))
                    .check(jsonPath("$[*].id").findRandom().optional().saveAs("targetProductId")))

            .exec(session -> {
                if (!session.contains("targetProductId")) {
                    return session.set("targetProductId", "1");
                }
                return session;
            })
            .pause(1)

            // Create Review
            // POST /api/products/{productId}/reviews
            // DTO: ReviewRequest
            .exec(http("Create Review")
                    .post("/api/products/#{targetProductId}/reviews")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{"
                            + "\"customerId\":\"#{reviewCustomerId}\","
                            + "\"rating\":5,"
                            + "\"comment\":\"Gatling automated review test.\""
                            + "}"))
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("createdReviewId"))
                    .check(jsonPath("$.productId").saveAs("createdProductId"))
                    .check(jsonPath("$.customerId").is("#{reviewCustomerId}")))
            .pause(1)

            // Get Review By Id
            // GET /api/reviews/{id}
            .exec(http("Get Review By Id")
                    .get("/api/reviews/#{createdReviewId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200))
                    .check(jsonPath("$.id").is("#{createdReviewId}")))
            .pause(1)

            // Get Reviews By Product
            .exec(http("Get Product Reviews")
                    .get("/api/products/#{createdProductId}/reviews")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .pause(1)

            // Update Review
            // PUT /api/reviews/{id}
            // DTO: ReviewRequest
            .exec(http("Update Review")
                    .put("/api/reviews/#{createdReviewId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{"
                            + "\"customerId\":\"#{reviewCustomerId}\","
                            + "\"rating\":4,"
                            + "\"comment\":\"Updated review text from Gatling test.\""
                            + "}"))
                    .check(status().is(200))
                    .check(jsonPath("$.rating").ofInt().is(4)))
            .pause(1)

            // Delete Review
            // DELETE /api/reviews/{id}
            .exec(http("Delete Review")
                    .delete("/api/reviews/#{createdReviewId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(204)))
            .pause(1)

            // Verify Review Deleted
            .exec(http("Get Deleted Review Should 404")
                    .get("/api/reviews/#{createdReviewId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(404)));

    {
        setUp(
                scn.injectOpen(atOnceUsers(1))
        ).protocols(httpProtocol);
    }
}