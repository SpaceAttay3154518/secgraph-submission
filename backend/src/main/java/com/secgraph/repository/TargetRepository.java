package com.secgraph.repository;

import com.secgraph.model.Target;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.Optional;

public interface TargetRepository extends Neo4jRepository<Target, Long> {

    @Query("MATCH (t:Target) RETURN t ORDER BY t.domain")
    java.util.List<Target> findAllTargets();

    Optional<Target> findByDomain(String domain);

    @Query("MATCH (t:Target {domain: $domain})-[:HAS_ENDPOINT]->(e:Endpoint) RETURN count(e)")
    long countEndpointsByDomain(String domain);

    @Query("""
        MATCH (t:Target {domain: $domain})-[:HAS_ENDPOINT]->(e:Endpoint)
        OPTIONAL MATCH (e)-[:ACCEPTS]->(p:Parameter)
        OPTIONAL MATCH (e)-[:POTENTIALLY_VULNERABLE_TO]->(v:VulnerabilityType)
        RETURN t, collect(DISTINCT e) AS endpoints
    """)
    Optional<Target> findTargetWithEndpoints(String domain);
}
