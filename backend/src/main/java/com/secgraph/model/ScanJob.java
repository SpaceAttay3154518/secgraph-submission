package com.secgraph.model;

import org.springframework.data.neo4j.core.schema.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Node
public class ScanJob {

    @Id
    @GeneratedValue
    private Long id;

    private String status;
    private Instant startedAt;
    private Instant completedAt;
    private String scanType;
    private String errorMessage;

    @Relationship(type = "DISCOVERED", direction = Relationship.Direction.OUTGOING)
    private List<Endpoint> discoveredEndpoints = new ArrayList<>();

    public ScanJob() {}

    public ScanJob(String scanType) {
        this.scanType = scanType;
        this.status = "PENDING";
        this.startedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public String getScanType() { return scanType; }
    public void setScanType(String scanType) { this.scanType = scanType; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public List<Endpoint> getDiscoveredEndpoints() { return discoveredEndpoints; }
    public void setDiscoveredEndpoints(List<Endpoint> discoveredEndpoints) { this.discoveredEndpoints = discoveredEndpoints; }
}
