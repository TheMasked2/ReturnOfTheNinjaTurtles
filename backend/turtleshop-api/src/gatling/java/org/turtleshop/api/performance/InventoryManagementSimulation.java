package org.turtleshop.api.performance;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import io.github.cdimascio.dotenv.Dotenv;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class InventoryManagementSimulation extends Simulation {

    // Load the .env file from the project root for Admin credentials
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("../../")
            .ignoreIfMissing()
            .load();

    private static final String adminEmail = dotenv.get("ADMIN_EMAIL", "default-admin@turtleshop.com");
    private static final String adminPassword = dotenv.get("ADMIN_PASSWORD", "default-password");

    // Global HTTP Configurations
    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // Define the Journey
    ScenarioBuilder scn = scenario("Admin Inventory Audit & Replenishment Journey")

            // 1. Log in securely as Admin
            .exec(http("Admin Login Request")
                    .post("/api/auth/login")
                    .body(StringBody(String.format("{\"email\":\"%s\",\"password\":\"%s\"}", adminEmail, adminPassword)))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("jwtToken")))
            .pause(1)

            // 2. Fetch inventory list
            .exec(http("Get All Inventory Stock")
                    .get("/api/inventory")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200))
                    // Fetch a random product ID from the inventory list to update next
                    .check(jsonPath("$[*].productId").findRandom().optional().saveAs("targetProductId")))
            .pause(1)
            
            // 3. Check for low stock items
            .exec(http("Get Low Stock Inventory")
                    .get("/api/inventory/low-stock?threshold=10")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))
            .pause(1)

            // 4. Replenish stock for the dynamically chosen product utilizing InventoryAdjustmentRequest DTO
            .doIf(session -> session.contains("targetProductId")).then(
                    exec(http("Restock Product Inventory")
                            .post("/api/inventory/product/#{targetProductId}/restock")
                            .header("Authorization", "Bearer #{jwtToken}")
                            .body(StringBody("{\"quantity\": 50}"))
                            .check(status().in(200, 204))) // Controller returns 204 No Content for success
            );

    // Traffic Injection Profile
    {
        setUp(
                scn.injectOpen(
                        nothingFor(2),
                        rampUsers(5).during(5),
                        constantUsersPerSec(10).during(15)
                )
        ).protocols(httpProtocol);
    }
}