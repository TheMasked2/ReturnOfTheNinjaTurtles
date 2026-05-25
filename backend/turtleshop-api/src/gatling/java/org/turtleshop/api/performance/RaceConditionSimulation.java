package org.turtleshop.api.performance;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.util.UUID;

public class RaceConditionSimulation extends Simulation {

    // Global HTTP Configurations
    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // Define the Journey
    ScenarioBuilder scn = scenario("High Concurrency Flash Sale Contention")
            // 1. Generate unique details per virtual user session
            .exec(session -> {
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                String dynamicEmail = "flash_ninja_" + uniqueId + "@turtleshop.com";
                return session
                        .set("dynamicEmail", dynamicEmail)
                        .set("dynamicPassword", "SecurePass123!");
            })

            // 2. Register the virtual user account
            .exec(http("Flash Sale Register")
                    .post("/api/auth/register")
                    .body(StringBody("{"
                            + "\"email\":\"#{dynamicEmail}\","
                            + "\"password\":\"#{dynamicPassword}\","
                            + "\"firstName\":\"Flash\","
                            + "\"lastName\":\"Turtle\","
                            + "\"phone\":\"+17166383774\""
                            + "}"))
                    .check(status().is(200)))
            .pause(1)

            // 3. Log the user in to capture their token and ID
            .exec(http("Flash Sale Login")
                    .post("/api/auth/login")
                    .body(StringBody("{\"email\":\"#{dynamicEmail}\",\"password\":\"#{dynamicPassword}\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("jwtToken"))
                    .check(jsonPath("$.customer.id").saveAs("currentCustomerId")))

            // 4. All create cart
            .exec(http("Flash Sale Create Cart")
                    .post("/api/cart/#{currentCustomerId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))

            // 5. Every single thread attempts to claim productId 1
            .exec(http("Contested Add to Cart")
                    .post("/api/cart/#{currentCustomerId}/items")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{\"productId\": 1, \"quantity\": 1}"))
                    .check(status().in(200, 201)))

            // 6. Every single user punches the order button simultaneously.
            .exec(http("Simultaneous Checkout Order")
                    .post("/api/checkout/customer/#{currentCustomerId}/place-order")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{"
                            + "\"shippingMethod\":\"PostNL\","
                            + "\"shippingAddress\":\"456 Shell Street, Amsterdam\","
                            + "\"paymentMethod\":\"Visa\""
                            + "}"))
                    // We accept 200 (Success) or 409/400 (due to low stock), and if the server returns 500 (Gatling flags KO).
                    .check(status().in(200, 400, 409)));

    // Traffic Injection Profile
    {
        setUp(
                scn.injectOpen(
                        nothingFor(2),
                        // We ramp up 30 users quickly so they arrive together at the Checkout step ready to compete
                        rampUsers(30).during(5)
                )
        ).protocols(httpProtocol);
    }
}