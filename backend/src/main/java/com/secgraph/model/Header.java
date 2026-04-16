package com.secgraph.model;

import org.springframework.data.neo4j.core.schema.*;

@Node
public class Header {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String value;

    public Header() {}

    public Header(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
