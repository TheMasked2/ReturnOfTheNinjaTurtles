package org.turtleshop.api.performance.query;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.util.UUID;

public class CustomerRegisterAndCheckoutSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Normal User Registration & Shopping Journey")
            // 1. Generate unique registration data dynamically per virtual user session
            .exec(session -> {
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                String dynamicEmail = "ninja_" + uniqueId + "@turtleshop.com";
                return session
                        .set("dynamicEmail", dynamicEmail)
                        .set("dynamicPassword", "SecurePass123!");
            })

            // 2. Register using the DYNAMIC session attributes
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
            .pause(1)

            // 3. Authenticate and extract nested customer ID from AuthResponse DTO
            .exec(http("Customer Login")
                    .post("/api/auth/login")
                    .body(StringBody("{\"email\":\"#{dynamicEmail}\",\"password\":\"#{dynamicPassword}\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("jwtToken"))
                    .check(jsonPath("$.customer.id").saveAs("currentCustomerId")))
            .pause(1)

            // NEW STEP: Explicitly create the cart container for the new customer session
            .exec(http("Create Empty Cart")
                    .post("/api/cart/#{currentCustomerId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .pause(1)

        // 4. Add items to the newly initialized cart container
            .exec(http("Add Product to Cart")
                    .post("/api/cart/#{currentCustomerId}/items")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{\"productId\": 1, \"quantity\": 1}"))
                    .check(status().in(200, 201)))
            .pause(1)

        // 5. View active shopping cart state
            .exec(http("Get Active Cart")
                    .get("/api/cart/#{currentCustomerId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .pause(1)

        // 6. Place Order via Checkout Controller
            .exec(http("Place Order via Checkout Controller")
                    .post("/api/checkout/customer/#{currentCustomerId}/place-order")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{"
                            + "\"shippingMethod\":\"PostNL\","
                            + "\"shippingAddress\":\"123 Sewer Lair, NYC\","
                            + "\"paymentMethod\":\"Visa\""
                            + "}"))
                    .check(status().is(200))
                    .check(jsonPath("$.orderId").saveAs("newOrderId"))
                    .check(jsonPath("$.totalAmount").saveAs("orderTotal")))
            .pause(2)

            // 7. Verify the final order state change
            .exec(http("View Order Details")
                    .get("/api/orders/#{newOrderId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)));

    {
        setUp(scn.injectOpen(rampUsers(20).during(10))).protocols(httpProtocol);
    }
}