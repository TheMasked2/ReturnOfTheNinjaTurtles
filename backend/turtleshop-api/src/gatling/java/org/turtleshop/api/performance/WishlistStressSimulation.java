package org.turtleshop.api.performance;

import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.regex;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class WishlistStressSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Wishlist E2E Stress Test")
            .exec(session -> {
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                return session
                        .set("dynamicEmail", "wishlist_user_" + uniqueId + "@turtleshop.com")
                        .set("dynamicPassword", "SecurePass123!");
            })

            .exec(http("Register Customer")
                    .post("/api/auth/register")
                    .body(StringBody("{"
                            + "\"email\":\"#{dynamicEmail}\","
                            + "\"password\":\"#{dynamicPassword}\","
                            + "\"firstName\":\"Casey\","
                            + "\"lastName\":\"Jones\","
                            + "\"phone\":\"+17165550123\""
                            + "}"))
                    .check(status().is(200)))
            .pause(1)

            .exec(http("Login Customer")
                    .post("/api/auth/login")
                    .body(StringBody("{\"email\":\"#{dynamicEmail}\",\"password\":\"#{dynamicPassword}\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("jwtToken"))
                    .check(jsonPath("$.customerId").optional().saveAs("currentCustomerId"))
                    .check(jsonPath("$.customer.id").optional().saveAs("currentCustomerId")))
            .pause(1)

            .exec(http("Create Wishlist")
                    .post("/api/wishlist/customer/#{currentCustomerId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200))
                    .check(regex("\\d+").saveAs("wishlistId")))
            .pause(1)

            .exec(http("Add First Product To Wishlist")
                    .post("/api/wishlist-item/wishlist/#{wishlistId}/product/1")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(204)))
            .pause(1)

            .exec(http("Add Second Product To Wishlist")
                    .post("/api/wishlist-item/wishlist/#{wishlistId}/product/2")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(204)))
            .pause(1)

            .exec(http("Get Wishlist Items")
                    .get("/api/wishlist-item/wishlist/#{wishlistId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .pause(1)

            .exec(http("Remove First Product From Wishlist")
                    .delete("/api/wishlist-item/wishlist/#{wishlistId}/product/1")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(204)))
            .pause(1)

            .exec(http("Verify Wishlist Item Removal")
                    .get("/api/wishlist-item/wishlist/#{wishlistId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .pause(1)

            .exec(http("Delete Wishlist")
                    .delete("/api/wishlist/#{wishlistId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(204)));

    {
        setUp(
                scn.injectOpen(
                        rampUsers(50).during(20),
                        constantUsersPerSec(15).during(30)
                )
        ).protocols(httpProtocol);
    }
}