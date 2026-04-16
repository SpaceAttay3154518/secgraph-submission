package com.secgraph.controller;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final Driver driver;

    public StatsController(Driver driver) {
        this.driver = driver;
    }

    @GetMapping("/{targetId}")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Long targetId) {
        Map<String, Object> stats = new HashMap<>();

        try (Session session = driver.session()) {
            var counts = session.run("""
                MATCH (t:Target) WHERE id(t) = $targetId
                OPTIONAL MATCH (t)-[:HAS_ENDPOINT]->(e:Endpoint)
                OPTIONAL MATCH (t)-[:USES]->(tech:Technology)
                OPTIONAL MATCH (tech)-[:AFFECTED_BY]->(cve:CVE)
                OPTIONAL MATCH (e)-[:ACCEPTS]->(p:Parameter)
                RETURN count(DISTINCT e) AS endpoints,
                       count(DISTINCT tech) AS technologies,
                       count(DISTINCT cve) AS cves,
                       count(DISTINCT p) AS parameters
            """, Map.of("targetId", targetId));

            if (counts.hasNext()) {
                Record r = counts.next();
                stats.put("endpoints", r.get("endpoints").asLong());
                stats.put("technologies", r.get("technologies").asLong());
                stats.put("cves", r.get("cves").asLong());
                stats.put("parameters", r.get("parameters").asLong());
            }

            var severities = session.run("""
                MATCH (t:Target)-[:USES]->(tech:Technology)-[:AFFECTED_BY]->(cve:CVE)
                WHERE id(t) = $targetId
                RETURN cve.severity AS severity, count(cve) AS count
            """, Map.of("targetId", targetId));

            Map<String, Long> severityBreakdown = new HashMap<>();
            while (severities.hasNext()) {
                Record r = severities.next();
                severityBreakdown.put(r.get("severity").asString("UNKNOWN"), r.get("count").asLong());
            }
            stats.put("severityBreakdown", severityBreakdown);

            var risky = session.run("""
                MATCH (t:Target)-[:HAS_ENDPOINT]->(e:Endpoint)
                WHERE id(t) = $targetId AND e.riskScore IS NOT NULL
                RETURN e.path AS path, e.method AS method, e.riskScore AS score
                ORDER BY e.riskScore DESC LIMIT 5
            """, Map.of("targetId", targetId));

            var topEndpoints = new java.util.ArrayList<Map<String, Object>>();
            while (risky.hasNext()) {
                Record r = risky.next();
                topEndpoints.add(Map.of(
                        "path", r.get("path").asString(""),
                        "method", r.get("method").asString(""),
                        "score", r.get("score").asDouble()
                ));
            }
            stats.put("topRiskEndpoints", topEndpoints);
        }

        return ResponseEntity.ok(stats);
    }
}
