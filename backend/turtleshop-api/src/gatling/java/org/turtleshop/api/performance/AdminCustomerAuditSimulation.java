package org.turtleshop.api.performance;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import io.github.cdimascio.dotenv.Dotenv;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class AdminCustomerAuditSimulation extends Simulation {

    // Load the .env file from the project root
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("../../")
            .ignoreIfMissing()
            .load();

    // Pull the credentials
    private static final String adminEmail = dotenv.get("ADMIN_EMAIL", "default-admin@turtleshop.com");
    private static final String adminPassword = dotenv.get("ADMIN_PASSWORD", "default-password");

    // Global HTTP Configurations
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // Define the Journey
    ScenarioBuilder scn = scenario("Admin Customer Management Load Test")

            // 1. Log in securely
            .exec(http("Admin Login Request")
                    .post("/api/auth/login")
                    // Constructing the JSON body using your injected environment variables safely
                    .body(StringBody(String.format("{\"email\":\"%s\",\"password\":\"%s\"}", adminEmail, adminPassword)))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("jwtToken")))

            .pause(1)

            // 2. Test the SQL view endpoint
            .exec(http("Get Order Summary View API")
                    .get("/api/orders/admin/summary?limit=50")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)))

            .pause(1)

            // 3. Fetch all customers using the extracted Token
            .exec(http("Get Customer Page API")
                    .get("/api/customer?page=0&size=20")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200))
                    .check(jsonPath("$.content[0].id").saveAs("dynamicCustomerId")))

            .exitHereIfFailed()

            .pause(1)

            .exec(http("Get Customer By ID API")
                    .get("/api/customer/#{dynamicCustomerId}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)));

    // Traffic Injection Profile
    {
        setUp(
                scn.injectOpen(
                        rampUsers(50).during(20),
                        constantUsersPerSec(15).during(30)
                )
        ).protocols(httpProtocol);
    }
}