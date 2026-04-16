package com.secgraph.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NvdResponse {

    private int totalResults;
    private List<Vulnerability> vulnerabilities;

    public int getTotalResults() { return totalResults; }
    public void setTotalResults(int totalResults) { this.totalResults = totalResults; }

    public List<Vulnerability> getVulnerabilities() { return vulnerabilities; }
    public void setVulnerabilities(List<Vulnerability> vulnerabilities) { this.vulnerabilities = vulnerabilities; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Vulnerability {
        private CveItem cve;

        public CveItem getCve() { return cve; }
        public void setCve(CveItem cve) { this.cve = cve; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CveItem {
        private String id;
        private List<Description> descriptions;
        private String published;
        private Metrics metrics;
        private List<Weakness> weaknesses;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public List<Description> getDescriptions() { return descriptions; }
        public void setDescriptions(List<Description> descriptions) { this.descriptions = descriptions; }

        public String getPublished() { return published; }
        public void setPublished(String published) { this.published = published; }

        public Metrics getMetrics() { return metrics; }
        public void setMetrics(Metrics metrics) { this.metrics = metrics; }

        public List<Weakness> getWeaknesses() { return weaknesses; }
        public void setWeaknesses(List<Weakness> weaknesses) { this.weaknesses = weaknesses; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Description {
        private String lang;
        private String value;

        public String getLang() { return lang; }
        public void setLang(String lang) { this.lang = lang; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metrics {
        private List<CvssV31> cvssMetricV31;

        public List<CvssV31> getCvssMetricV31() { return cvssMetricV31; }
        public void setCvssMetricV31(List<CvssV31> cvssMetricV31) { this.cvssMetricV31 = cvssMetricV31; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CvssV31 {
        private CvssData cvssData;

        public CvssData getCvssData() { return cvssData; }
        public void setCvssData(CvssData cvssData) { this.cvssData = cvssData; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CvssData {
        private Double baseScore;
        private String baseSeverity;

        public Double getBaseScore() { return baseScore; }
        public void setBaseScore(Double baseScore) { this.baseScore = baseScore; }

        public String getBaseSeverity() { return baseSeverity; }
        public void setBaseSeverity(String baseSeverity) { this.baseSeverity = baseSeverity; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weakness {
        private List<WeaknessDescription> description;

        public List<WeaknessDescription> getDescription() { return description; }
        public void setDescription(List<WeaknessDescription> description) { this.description = description; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeaknessDescription {
        private String lang;
        private String value;

        public String getLang() { return lang; }
        public void setLang(String lang) { this.lang = lang; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
