package com.secgraph.repository;

import com.secgraph.model.ScanJob;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface ScanJobRepository extends Neo4jRepository<ScanJob, Long> {

    @Query("""
        MATCH (t:Target {domain: $domain})-[:SCANNED_BY]->(s:ScanJob)
        RETURN s ORDER BY s.startedAt DESC
    """)
    List<ScanJob> findByTargetDomain(String domain);

    List<ScanJob> findByStatus(String status);
}
