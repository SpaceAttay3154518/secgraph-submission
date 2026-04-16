package com.secgraph.dto;

import java.util.List;

public class ReconResult {

    private List<DiscoveredEndpoint> endpoints;
    private List<DiscoveredTechnology> technologies;
    private HeaderAnalysis headerAnalysis;

    public List<DiscoveredEndpoint> getEndpoints() { return endpoints; }
    public void setEndpoints(List<DiscoveredEndpoint> endpoints) { this.endpoints = endpoints; }

    public List<DiscoveredTechnology> getTechnologies() { return technologies; }
    public void setTechnologies(List<DiscoveredTechnology> technologies) { this.technologies = technologies; }

    public HeaderAnalysis getHeaderAnalysis() { return headerAnalysis; }
    public void setHeaderAnalysis(HeaderAnalysis headerAnalysis) { this.headerAnalysis = headerAnalysis; }

    public static class DiscoveredEndpoint {
        private String path;
        private String method;
        private Integer statusCode;
        private String contentType;
        private List<DiscoveredParam> params;

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public Integer getStatusCode() { return statusCode; }
        public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public List<DiscoveredParam> getParams() { return params; }
        public void setParams(List<DiscoveredParam> params) { this.params = params; }
    }

    public static class DiscoveredParam {
        private String name;
        private String type;
        private String location;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class DiscoveredTechnology {
        private String name;
        private String version;
        private String category;
        private Double confidence;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
    }

    public static class HeaderAnalysis {
        private java.util.Map<String, String> headers;
        private Integer score;
        private List<String> issues;

        public java.util.Map<String, String> getHeaders() { return headers; }
        public void setHeaders(java.util.Map<String, String> headers) { this.headers = headers; }

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }

        public List<String> getIssues() { return issues; }
        public void setIssues(List<String> issues) { this.issues = issues; }
    }
}
