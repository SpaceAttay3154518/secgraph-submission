package com.secgraph.model;

import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;

@Node
public class Parameter {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String type;
    private String location;

    @Relationship(type = "SUSCEPTIBLE_TO", direction = Relationship.Direction.OUTGOING)
    private List<VulnerabilityType> susceptibleTo = new ArrayList<>();

    public Parameter() {}

    public Parameter(String name, String type, String location) {
        this.name = name;
        this.type = type;
        this.location = location;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<VulnerabilityType> getSusceptibleTo() { return susceptibleTo; }
    public void setSusceptibleTo(List<VulnerabilityType> susceptibleTo) { this.susceptibleTo = susceptibleTo; }
}
