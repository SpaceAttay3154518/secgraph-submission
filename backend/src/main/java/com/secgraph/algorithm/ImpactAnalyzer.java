package com.secgraph.algorithm;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ImpactAnalyzer {

    private final Driver driver;

    public ImpactAnalyzer(Driver driver) {
        this.driver = driver;
    }
    public List<EndpointImpact> computeCentrality(Long targetId, int limit) {
        String cypher = """
            MATCH (t:Target)-[:HAS_ENDPOINT]->(e:Endpoint)
            WHERE id(t) = $targetId
            WITH collect(e) AS endpoints
            UNWIND endpoints AS src
            UNWIND endpoints AS dst
            WITH src, dst WHERE id(src) < id(dst)
            MATCH path = shortestPath((src)-[*..5]-(dst))
            WITH nodes(path) AS pathNodes
            UNWIND pathNodes AS n
            WITH n WHERE n:Endpoint
            RETURN id(n) AS nodeId, n.path AS path, n.method AS method,
                   count(*) AS pathCount
            ORDER BY pathCount DESC
            LIMIT $limit
        """;

        List<EndpointImpact> results = new ArrayList<>();

        try (Session session = driver.session()) {
            var result = session.run(cypher, Map.of("targetId", targetId, "limit", limit));
            while (result.hasNext()) {
                Record record = result.next();
                results.add(new EndpointImpact(
                        record.get("nodeId").asLong(),
                        record.get("path").asString(""),
                        record.get("method").asString("GET"),
                        record.get("pathCount").asLong(),
                        0.0
                ));
            }
        }

        if (!results.isEmpty()) {
            long maxCount = results.get(0).rawPathCount();
            results = results.stream()
                    .map(r -> new EndpointImpact(
                            r.endpointId(), r.path(), r.method(), r.rawPathCount(),
                            maxCount > 0 ? (r.rawPathCount() * 10.0) / maxCount : 0.0
                    ))
                    .toList();
        }

        return results;
    }
    public List<FlowAnalysis> analyzeDataFlow(Long targetId) {
        String cypher = """
            MATCH (t:Target)-[:HAS_ENDPOINT]->(e:Endpoint)
            WHERE id(t) = $targetId
            OPTIONAL MATCH (e)-[:FLOWS_TO]->(out:Endpoint)
            OPTIONAL MATCH (in:Endpoint)-[:FLOWS_TO]->(e)
            RETURN id(e) AS nodeId, e.path AS path, e.method AS method,
                   count(DISTINCT out) AS outDegree,
                   count(DISTINCT in) AS inDegree
            ORDER BY (count(DISTINCT out) + count(DISTINCT in)) DESC
        """;

        List<FlowAnalysis> results = new ArrayList<>();

        try (Session session = driver.session()) {
            var result = session.run(cypher, Map.of("targetId", targetId));
            while (result.hasNext()) {
                Record record = result.next();
                results.add(new FlowAnalysis(
                        record.get("nodeId").asLong(),
                        record.get("path").asString(""),
                        record.get("method").asString("GET"),
                        record.get("outDegree").asLong(),
                        record.get("inDegree").asLong()
                ));
            }
        }

        return results;
    }

    public record EndpointImpact(
            long endpointId, String path, String method,
            long rawPathCount, double normalizedScore
    ) {}

    public record FlowAnalysis(
            long endpointId, String path, String method,
            long outDegree, long inDegree
    ) {}
}
