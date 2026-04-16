package com.secgraph.model;

import org.springframework.data.neo4j.core.schema.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Node
public class Endpoint {

    @Id
    @GeneratedValue
    private Long id;

    private String path;
    private String method;
    private Integer statusCode;
    private String contentType;
    private Instant discoveredAt;
    private Double riskScore;

    @Relationship(type = "ACCEPTS", direction = Relationship.Direction.OUTGOING)
    private List<Parameter> parameters = new ArrayList<>();

    @Relationship(type = "FLOWS_TO", direction = Relationship.Direction.OUTGOING)
    private List<Endpoint> flowsTo = new ArrayList<>();

    @Relationship(type = "POTENTIALLY_VULNERABLE_TO", direction = Relationship.Direction.OUTGOING)
    private List<VulnerabilityType> vulnerabilities = new ArrayList<>();

    @Relationship(type = "RETURNS", direction = Relationship.Direction.OUTGOING)
    private List<Header> headers = new ArrayList<>();

    public Endpoint() {}

    public Endpoint(String path, String method) {
        this.path = path;
        this.method = method;
        this.discoveredAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Instant getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(Instant discoveredAt) { this.discoveredAt = discoveredAt; }

    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }

    public List<Parameter> getParameters() { return parameters; }
    public void setParameters(List<Parameter> parameters) { this.parameters = parameters; }

    public List<Endpoint> getFlowsTo() { return flowsTo; }
    public void setFlowsTo(List<Endpoint> flowsTo) { this.flowsTo = flowsTo; }

    public List<VulnerabilityType> getVulnerabilities() { return vulnerabilities; }
    public void setVulnerabilities(List<VulnerabilityType> vulnerabilities) { this.vulnerabilities = vulnerabilities; }

    public List<Header> getHeaders() { return headers; }
    public void setHeaders(List<Header> headers) { this.headers = headers; }
}
