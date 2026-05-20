package org.turtleshop.api.performance;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class CatalogSearchAndPaginationSimulation extends Simulation {

    // Global HTTP Configurations
    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // Define the Journey
    ScenarioBuilder scn = scenario("Catalog Browsing and Detail Lookup Load Test")

            // 1. Initial page fetch with sorting constraints
            .exec(http("Browse Catalog Page 1 - Price Ascending")
                    .get("/api/products?page=0&size=10&sort=price,asc")
                    .check(status().is(200)))
            .pause(1)

            // 2. Next page tracking to verify pagination layouts
            .exec(http("Browse Catalog Page 2 - Price Descending")
                    .get("/api/products?page=0&size=10&sort=price,desc")
                    .check(status().is(200))
                    .check(jsonPath("$[*].id").findRandom().optional().saveAs("browsedProductId")))
            .pause(2)

            // 3. Search: Hits the valid base route with a filter parameter
            .exec(http("Search Products by Term Snapshot")
                    .get("/api/products?search=Turtle")
                    .check(status().is(200)))
            .pause(1)

            // 4. Targeted item detail lookup via unique ID
            .doIf(session -> session.contains("browsedProductId")).then(
                    exec(http("View Product Details")
                            .get("/api/products/#{browsedProductId}")
                            .check(status().is(200)))
            );

    // Traffic Injection Profile
    {
        setUp(
                scn.injectOpen(
                        nothingFor(2),
                        rampUsers(15).during(5),
                        constantUsersPerSec(20).during(30)
                )
        ).protocols(httpProtocol);
    }
}