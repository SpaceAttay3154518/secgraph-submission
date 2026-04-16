package com.secgraph.model;

import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;

@Node
public class Technology {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String version;
    private String category;
    private Double confidence;

    @Relationship(type = "AFFECTED_BY", direction = Relationship.Direction.OUTGOING)
    private List<Cve> cves = new ArrayList<>();

    public Technology() {}

    public Technology(String name, String version, String category) {
        this.name = name;
        this.version = version;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public List<Cve> getCves() { return cves; }
    public void setCves(List<Cve> cves) { this.cves = cves; }
}
