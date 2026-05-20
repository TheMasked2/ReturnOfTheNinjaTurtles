package org.turtleshop.api.performance;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.util.UUID;

public class CustomerRegisterAndCheckoutSimulation extends Simulation {

    // Global HTTP Configurations
    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // Define the Journey
    ScenarioBuilder scn = scenario("Normal User Registration & Shopping Journey")
            // 1. Generate registration data per virtual user session
            .exec(session -> {
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                String dynamicEmail = "ninja_" + uniqueId + "@turtleshop.com";
                return session
                        .set("dynamicEmail", dynamicEmail)
                        .set("dynamicPassword", "SecurePass123!");
            })

            // 2. Register using the session attributes
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

            // 3. Log in and extract nested customer ID from AuthResponse DTO
            .exec(http("Customer Login")
                    .post("/api/auth/login")
                    .body(StringBody("{\"email\":\"#{dynamicEmail}\",\"password\":\"#{dynamicPassword}\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("jwtToken"))
                    .check(jsonPath("$.customer.id").saveAs("currentCustomerId")))
            .pause(1)

            // 4. Create the cart
            .exec(http("Create Empty Cart")
                    .post("/api/cart/#{currentCustomerId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .pause(1)

            // 5. Add items to the newly initialized cart container
            .exec(http("Add Product to Cart")
                    .post("/api/cart/#{currentCustomerId}/items")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(StringBody("{\"productId\": 1, \"quantity\": 1}"))
                    .check(status().in(200, 201)))
            .pause(1)

            // 6. View active shopping cart state
            .exec(http("Get Active Cart")
                    .get("/api/cart/#{currentCustomerId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .pause(1)

            // 7. Place Order
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

            // 8. Verify the final order state change
            .exec(http("View Order Details")
                    .get("/api/orders/#{newOrderId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)));

    // Traffic Injection Profile
    {
        setUp(scn.injectOpen(rampUsers(20).during(10))).protocols(httpProtocol);
    }
}