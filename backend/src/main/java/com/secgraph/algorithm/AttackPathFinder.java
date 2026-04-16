package com.secgraph.algorithm;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AttackPathFinder {

    private final Driver driver;

    public AttackPathFinder(Driver driver) {
        this.driver = driver;
    }
    public List<AttackPath> findAttackPaths(Long targetId, double minCvss, int limit) {
        String cypher = """
            MATCH (t:Target)-[:HAS_ENDPOINT]->(entry:Endpoint)
            WHERE id(t) = $targetId
            WITH entry
            MATCH (tech:Technology)-[:AFFECTED_BY]->(cve:CVE)
            WHERE cve.cvssScore >= $minCvss
            WITH entry, cve, tech
            MATCH path = shortestPath((entry)-[*..6]-(cve))
            WITH path, entry, cve,
                 reduce(risk = 0.0, n IN nodes(path) |
                   risk + CASE
                     WHEN n:CVE THEN coalesce(n.cvssScore, 0) / 2.0
                     WHEN n:Endpoint THEN coalesce(n.riskScore, 1.0)
                     WHEN n:VulnerabilityType THEN 2.0
                     ELSE 0.5
                   END
                 ) AS totalRisk
            RETURN path, totalRisk, entry.path AS entryPoint, cve.cveId AS cveId, cve.cvssScore AS cvss
            ORDER BY totalRisk DESC
            LIMIT $limit
        """;

        List<AttackPath> paths = new ArrayList<>();

        try (Session session = driver.session()) {
            var result = session.run(cypher, Map.of(
                    "targetId", targetId,
                    "minCvss", minCvss,
                    "limit", limit
            ));

            while (result.hasNext()) {
                Record record = result.next();
                Path neoPath = record.get("path").asPath();
                double totalRisk = record.get("totalRisk").asDouble();
                String entryPoint = record.get("entryPoint").asString();
                String cveId = record.get("cveId").asString();
                double cvss = record.get("cvss").asDouble();

                List<PathNode> nodeChain = new ArrayList<>();
                for (Node node : neoPath.nodes()) {
                    String type = node.labels().iterator().next();
                    String label = switch (type) {
                        case "Endpoint" -> node.get("method").asString("") + " " + node.get("path").asString("");
                        case "Technology" -> node.get("name").asString("");
                        case "CVE" -> node.get("cveId").asString("");
                        case "VulnerabilityType" -> node.get("name").asString("");
                        case "Parameter" -> node.get("name").asString("");
                        default -> type;
                    };
                    nodeChain.add(new PathNode(node.elementId(), type.toLowerCase(), label));
                }

                paths.add(new AttackPath(nodeChain, totalRisk, entryPoint, cveId, cvss));
            }
        }

        return paths;
    }
    public BlastRadius findBlastRadius(Long endpointId, int maxDepth) {
        String cypher = """
            MATCH (start:Endpoint) WHERE id(start) = $endpointId
            CALL {
                WITH start
                MATCH path = (start)-[:FLOWS_TO*1..%d]->(reached:Endpoint)
                RETURN reached, length(path) AS depth
            }
            RETURN reached.path AS path, reached.method AS method, depth
            ORDER BY depth
        """.formatted(maxDepth);

        List<ReachableNode> reachable = new ArrayList<>();

        try (Session session = driver.session()) {
            var result = session.run(cypher, Map.of("endpointId", endpointId));
            while (result.hasNext()) {
                Record record = result.next();
                reachable.add(new ReachableNode(
                        record.get("path").asString(""),
                        record.get("method").asString("GET"),
                        record.get("depth").asInt()
                ));
            }
        }

        return new BlastRadius(endpointId, reachable);
    }

    public record AttackPath(
            List<PathNode> nodes,
            double totalRisk,
            String entryPoint,
            String cveId,
            double cvssScore
    ) {}

    public record PathNode(String id, String type, String label) {}

    public record BlastRadius(Long sourceEndpointId, List<ReachableNode> reachable) {}

    public record ReachableNode(String path, String method, int depth) {}
}
