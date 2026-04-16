package com.secgraph.model;

import org.springframework.data.neo4j.core.schema.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Node
public class Target {

    @Id
    @GeneratedValue
    private Long id;

    private String domain;
    private String ip;
    private Instant firstSeen;
    private Instant lastSeen;

    @Relationship(type = "HAS_ENDPOINT", direction = Relationship.Direction.OUTGOING)
    private List<Endpoint> endpoints = new ArrayList<>();

    @Relationship(type = "USES", direction = Relationship.Direction.OUTGOING)
    private List<Technology> technologies = new ArrayList<>();

    @Relationship(type = "SCANNED_BY", direction = Relationship.Direction.OUTGOING)
    private List<ScanJob> scanJobs = new ArrayList<>();

    public Target() {}

    public Target(String domain) {
        this.domain = domain;
        this.firstSeen = Instant.now();
        this.lastSeen = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public Instant getFirstSeen() { return firstSeen; }
    public void setFirstSeen(Instant firstSeen) { this.firstSeen = firstSeen; }

    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }

    public List<Endpoint> getEndpoints() { return endpoints; }
    public void setEndpoints(List<Endpoint> endpoints) { this.endpoints = endpoints; }

    public List<Technology> getTechnologies() { return technologies; }
    public void setTechnologies(List<Technology> technologies) { this.technologies = technologies; }

    public List<ScanJob> getScanJobs() { return scanJobs; }
    public void setScanJobs(List<ScanJob> scanJobs) { this.scanJobs = scanJobs; }
}
