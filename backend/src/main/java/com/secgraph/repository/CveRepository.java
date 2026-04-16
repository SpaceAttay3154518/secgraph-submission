package com.secgraph.repository;

import com.secgraph.model.Cve;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface CveRepository extends Neo4jRepository<Cve, Long> {

    Optional<Cve> findByCveId(String cveId);

    @Query("""
        MATCH (t:Target {domain: $domain})-[:USES]->(tech:Technology)-[:AFFECTED_BY]->(c:CVE)
        RETURN c ORDER BY c.cvssScore DESC
    """)
    List<Cve> findByTargetDomain(String domain);

    @Query("""
        MATCH (c:CVE) WHERE c.severity = $severity
        RETURN c ORDER BY c.cvssScore DESC
    """)
    List<Cve> findBySeverity(String severity);
}
