package com.secgraph.algorithm;

import com.secgraph.model.Endpoint;
import com.secgraph.repository.EndpointRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AttackSurfaceScorer {

    private final Driver driver;
    private final EndpointRepository endpointRepository;

    private static final double PARAM_WEIGHT = 0.30;
    private static final double CVE_WEIGHT = 0.35;
    private static final double FLOW_WEIGHT = 0.15;
    private static final double HEADER_WEIGHT = 0.10;
    private static final double VULN_WEIGHT = 0.10;

    public AttackSurfaceScorer(Driver driver, EndpointRepository endpointRepository) {
        this.driver = driver;
        this.endpointRepository = endpointRepository;
    }
    public List<EndpointScore> scoreEndpoints(Long targetId) {
        String cypher = """
            MATCH (t:Target)-[:HAS_ENDPOINT]->(e:Endpoint)
            WHERE id(t) = $targetId
            OPTIONAL MATCH (e)-[:ACCEPTS]->(p:Parameter)
            OPTIONAL MATCH (e)-[:POTENTIALLY_VULNERABLE_TO]->(v:VulnerabilityType)
            OPTIONAL MATCH (e)-[:FLOWS_TO]->(flow:Endpoint)
            OPTIONAL MATCH (in:Endpoint)-[:FLOWS_TO]->(e)
            OPTIONAL MATCH (e)-[:RETURNS]->(h:Header)
            WITH e,
                 count(DISTINCT p) AS paramCount,
                 count(DISTINCT v) AS vulnCount,
                 count(DISTINCT flow) + count(DISTINCT in) AS flowDegree,
                 count(DISTINCT h) AS headerCount
            OPTIONAL MATCH (t:Target)-[:USES]->(tech:Technology)-[:AFFECTED_BY]->(cve:CVE)
            WHERE id(t) = $targetId
            WITH e, paramCount, vulnCount, flowDegree, headerCount,
                 count(DISTINCT cve) AS relatedCves
            RETURN id(e) AS nodeId, e.path AS path, e.method AS method,
                   paramCount, vulnCount, flowDegree, headerCount, relatedCves
        """;

        List<EndpointScore> scores = new ArrayList<>();

        try (Session session = driver.session()) {
            var result = session.run(cypher, Map.of("targetId", targetId));

            double maxRaw = 0;
            List<RawScore> rawScores = new ArrayList<>();

            while (result.hasNext()) {
                Record record = result.next();
                long nodeId = record.get("nodeId").asLong();
                String path = record.get("path").asString("");
                String method = record.get("method").asString("GET");
                long paramCount = record.get("paramCount").asLong();
                long vulnCount = record.get("vulnCount").asLong();
                long flowDegree = record.get("flowDegree").asLong();
                long headerCount = record.get("headerCount").asLong();
                long relatedCves = record.get("relatedCves").asLong();

                long missingHeaders = Math.max(0, 8 - headerCount);

                double raw = (paramCount * PARAM_WEIGHT)
                        + (relatedCves * CVE_WEIGHT)
                        + (flowDegree * FLOW_WEIGHT)
                        + (missingHeaders * HEADER_WEIGHT)
                        + (vulnCount * VULN_WEIGHT);

                if (raw > maxRaw) maxRaw = raw;

                rawScores.add(new RawScore(nodeId, path, method, raw, paramCount, vulnCount, flowDegree, relatedCves));
            }

            final double maxRawFinal = maxRaw;
            for (RawScore rs : rawScores) {
                final double normalized = Math.round(
                        (maxRawFinal > 0 ? (rs.raw / maxRawFinal) * 10.0 : 0.0) * 10.0) / 10.0;

                scores.add(new EndpointScore(rs.nodeId, rs.path, rs.method, normalized,
                        rs.paramCount, rs.vulnCount, rs.flowDegree, rs.relatedCves));

                endpointRepository.findById(rs.nodeId).ifPresent(ep -> {
                    ep.setRiskScore(normalized);
                    endpointRepository.save(ep);
                });
            }
        }

        scores.sort((a, b) -> Double.compare(b.score(), a.score()));
        return scores;
    }

    private record RawScore(long nodeId, String path, String method, double raw,
                            long paramCount, long vulnCount, long flowDegree, long relatedCves) {}

    public record EndpointScore(
            long endpointId, String path, String method, double score,
            long paramCount, long vulnCount, long flowDegree, long relatedCves
    ) {}
}
