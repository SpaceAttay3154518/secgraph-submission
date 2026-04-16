package com.secgraph.repository;

import com.secgraph.model.Technology;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface TechnologyRepository extends Neo4jRepository<Technology, Long> {

    Optional<Technology> findByNameAndVersion(String name, String version);

    @Query("""
        MATCH (t:Target {domain: $domain})-[:USES]->(tech:Technology)
        RETURN tech
    """)
    List<Technology> findByTargetDomain(String domain);

    @Query("""
        MATCH (tech:Technology)-[:AFFECTED_BY]->(c:CVE)
        WHERE c.cvssScore >= $minScore
        RETURN DISTINCT tech
    """)
    List<Technology> findWithCriticalCves(double minScore);
}
