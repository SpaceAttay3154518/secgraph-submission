package com.secgraph.model;

import org.springframework.data.neo4j.core.schema.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Node("CVE")
public class Cve {

    @Id
    @GeneratedValue
    private Long id;

    private String cveId;
    private Double cvssScore;
    private String severity;
    private String description;
    private LocalDate publishedDate;

    @Relationship(type = "EXPLOITS", direction = Relationship.Direction.OUTGOING)
    private List<VulnerabilityType> exploits = new ArrayList<>();

    public Cve() {}

    public Cve(String cveId, Double cvssScore, String severity, String description) {
        this.cveId = cveId;
        this.cvssScore = cvssScore;
        this.severity = severity;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCveId() { return cveId; }
    public void setCveId(String cveId) { this.cveId = cveId; }

    public Double getCvssScore() { return cvssScore; }
    public void setCvssScore(Double cvssScore) { this.cvssScore = cvssScore; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }

    public List<VulnerabilityType> getExploits() { return exploits; }
    public void setExploits(List<VulnerabilityType> exploits) { this.exploits = exploits; }
}
