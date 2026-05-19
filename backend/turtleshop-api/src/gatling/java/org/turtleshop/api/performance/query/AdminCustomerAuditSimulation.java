package org.turtleshop.api.performance.query;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import io.github.cdimascio.dotenv.Dotenv;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class AdminCustomerAuditSimulation extends Simulation {

    // Load the .env file from the project root
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("../../") // Back out of backend/turtleshop-api to reach absolute root
            .ignoreIfMissing()
            .load();

    // Pull the credentials safely, falling back to a default if the file is missing
    private static final String adminEmail = dotenv.get("ADMIN_EMAIL", "default-admin@turtleshop.com");
    private static final String adminPassword = dotenv.get("ADMIN_PASSWORD", "default-password");

    // 1. Core Global HTTP Settings
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // 2. Define the User Journey
    ScenarioBuilder scn = scenario("Admin Customer Management Load Test")

            // STEP 1: Log in securely using our dynamic variables
            .exec(http("Admin Login Request")
                    .post("/api/auth/login")
                    // Constructing the JSON body using your injected environment variables safely
                    .body(StringBody(String.format("{\"email\":\"%s\",\"password\":\"%s\"}", adminEmail, adminPassword)))
                    .check(status().is(200))
                    .check(jsonPath("$.token").saveAs("jwtToken")))

            .pause(1)

            // STEP 2: Fetch all customers using the extracted Token
            .exec(http("Get All Customers API")
                    .get("/api/customer")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200))
                    // $[0].id means: "Go to the first object in the array, grab the 'id' field"
                    // If your DTO uses a field named 'uuid' instead of 'id', change this to $.uuid
                    .check(jsonPath("$[0].id").saveAs("dynamicCustomerId")))

            .pause(1)

            // STEP 3: Fetch a single specific customer using the dynamically captured ID
            .exec(http("Get Customer By ID API")
                    .get("/api/customer/#{dynamicCustomerId}") // Safely injected here!
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(200)));

    // 3. Traffic Load Profile
    {
        setUp(
                scn.injectOpen(
                        nothingFor(2),
                        rampUsers(10).during(5),
                        constantUsersPerSec(20).during(15)
                )
        ).protocols(httpProtocol);
    }
}