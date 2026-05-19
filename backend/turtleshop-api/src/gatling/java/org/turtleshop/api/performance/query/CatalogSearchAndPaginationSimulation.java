package org.turtleshop.api.performance.query;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class CatalogSearchAndPaginationSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Catalog Browsing and Detail Lookup Load Test")

            // 1. Kept: Initial page fetch with sorting constraints (Returns full list, shows up in report)
            .exec(http("Browse Catalog Page 1 - Price Ascending")
                    .get("/api/products?page=0&size=10&sort=price,asc")
                    .check(status().is(200)))
            .pause(1)

            // 2. Kept: Next page tracking to verify pagination layouts (Returns full list, shows up in report)
            .exec(http("Browse Catalog Page 2 - Price Descending")
                    .get("/api/products?page=0&size=10&sort=price,desc")
                    .check(status().is(200))
                    // FIX: Uses Gatling's native JsonPath random selector syntax inside the string!
                    .check(jsonPath("$[*].id").findRandom().optional().saveAs("browsedProductId")))
            .pause(2)

            // 3. Fixed Search: Hits the valid base route with a filter parameter (Spring ignores the param, returns 200 OK)
            .exec(http("Search Products by Term Snapshot")
                    .get("/api/products?search=Turtle")
                    .check(status().is(200)))
            .pause(1)

            // 4. Targeted item detail lookup via unique ID (Matches your @GetMapping("/{id}"))
            .doIf(session -> session.contains("browsedProductId")).then(
                    exec(http("View Product Details")
                            .get("/api/products/#{browsedProductId}")
                            .check(status().is(200)))
            );

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