package org.turtleshop.api.performance.query;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class DatabaseOptimizationSimulation extends Simulation {

    // 1. Define where your Spring Boot app is running
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080") // Change port if your app uses something else (e.g., 8081)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // 2. Define the User Behavior
    // Replace these URIs with your actual project API endpoints
    ScenarioBuilder scn = scenario("Database Optimization Load Test")
            .exec(http("Unoptimized Query API")
                    .get("/api/turtles/unoptimized")
                    .check(status().is(200)))
            .pause(1) // 1 second think-time between requests
            .exec(http("Optimized Query API")
                    .get("/api/turtles/optimized")
                    .check(status().is(200)));

    // 3. Define the Traffic Load Profile
    {
        setUp(
                scn.injectOpen(
                        nothingFor(2),                  // 1. Let the system settle for 2 seconds
                        rampUsers(10).during(5),        // 2. Smoothly ramp up 10 users over 5 seconds
                        constantUsersPerSec(20).during(15) // 3. Keep a steady load of 20 users/sec for 15 seconds
                )
        ).protocols(httpProtocol);
    }
}