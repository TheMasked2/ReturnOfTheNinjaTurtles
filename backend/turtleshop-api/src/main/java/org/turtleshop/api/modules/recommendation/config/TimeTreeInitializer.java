package org.turtleshop.api.modules.recommendation.config;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class TimeTreeInitializer {

    private final Driver driver;

    public TimeTreeInitializer(Driver driver) {
        this.driver = driver;
    }

    @PostConstruct
    public void buildTemporalBackbone() {
        try (Session session = this.driver.session()) {
            // 1. Establish the static Season nodes
            session.run("UNWIND ['Winter', 'Spring', 'Summer', 'Autumn'] AS seasonName " +
                        "MERGE (s:Season {id: seasonName})");

            // 2. Generate Month nodes for 2024-2026 and link them to Seasons
            session.run("UNWIND range(2024, 2026) AS year " +
                        "UNWIND range(1, 12) AS month " +
                        "WITH year, month, " +
                        "     CASE " +
                        "       WHEN month IN [12, 1, 2] THEN 'Winter' " +
                        "       WHEN month IN [3, 4, 5] THEN 'Spring' " +
                        "       WHEN month IN [6, 7, 8] THEN 'Summer' " +
                        "       ELSE 'Autumn' " +
                        "     END AS seasonName " +
                        "WITH year, month, seasonName, " +
                        "     toString(year) + '-' + apoc.text.lpad(toString(month), 2, '0') AS monthId " +
                        "MERGE (m:Month {id: monthId, year: year, month: month}) " +
                        "WITH m, seasonName " +
                        "MATCH (s:Season {id: seasonName}) " +
                        "MERGE (m)-[:PART_OF_SEASON]->(s)");
        }
    }
}