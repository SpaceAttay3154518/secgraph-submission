package com.secgraph.repository;

import com.secgraph.model.Endpoint;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface EndpointRepository extends Neo4jRepository<Endpoint, Long> {

    @Query("""
        MATCH (t:Target {domain: $domain})-[:HAS_ENDPOINT]->(e:Endpoint)
        RETURN e ORDER BY e.riskScore DESC
    """)
    List<Endpoint> findByTargetDomain(String domain);

    @Query("""
        MATCH (t:Target)-[:HAS_ENDPOINT]->(e:Endpoint)
        WHERE id(t) = $targetId AND e.path = $path AND e.method = $method
        RETURN e
    """)
    List<Endpoint> findByTargetIdAndPathAndMethod(Long targetId, String path, String method);

    @Query("""
        MATCH (e:Endpoint)
        WHERE e.riskScore IS NOT NULL
        RETURN e ORDER BY e.riskScore DESC LIMIT $limit
    """)
    List<Endpoint> findTopRiskiest(int limit);
}
