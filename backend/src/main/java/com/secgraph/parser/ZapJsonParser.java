package com.secgraph.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ZapJsonParser {

    public static List<ZapAlert> parse(InputStream json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        List<ZapAlert> alerts = new ArrayList<>();

        JsonNode alertsNode = root.has("alerts") ? root.get("alerts") : root;

        if (alertsNode.isArray()) {
            for (JsonNode alert : alertsNode) {
                ZapAlert zapAlert = new ZapAlert();
                zapAlert.setAlert(textOrNull(alert, "alert"));
                zapAlert.setRisk(textOrNull(alert, "risk"));
                zapAlert.setConfidence(textOrNull(alert, "confidence"));
                zapAlert.setDescription(textOrNull(alert, "description"));
                zapAlert.setSolution(textOrNull(alert, "solution"));
                zapAlert.setParam(textOrNull(alert, "param"));

                String url = textOrNull(alert, "url");
                if (url != null) {
                    try {
                        URI uri = new URI(url);
                        zapAlert.setPath(uri.getPath() != null ? uri.getPath() : "/");
                    } catch (Exception e) {
                        zapAlert.setPath("/");
                    }
                }

                String cweId = textOrNull(alert, "cweid");
                if (cweId != null && !cweId.isBlank()) {
                    zapAlert.setCweId("CWE-" + cweId);
                }

                if (alert.has("instances") && alert.get("instances").isArray()) {
                    List<String> urls = new ArrayList<>();
                    for (JsonNode instance : alert.get("instances")) {
                        String instanceUrl = textOrNull(instance, "uri");
                        if (instanceUrl != null) urls.add(instanceUrl);
                    }
                    zapAlert.setInstanceUrls(urls);
                }

                alerts.add(zapAlert);
            }
        }

        return alerts;
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return child != null && !child.isNull() ? child.asText() : null;
    }

    public static class ZapAlert {
        private String alert;
        private String risk;
        private String confidence;
        private String description;
        private String solution;
        private String url;
        private String path;
        private String param;
        private String cweId;
        private List<String> instanceUrls;

        public String getAlert() { return alert; }
        public void setAlert(String alert) { this.alert = alert; }

        public String getRisk() { return risk; }
        public void setRisk(String risk) { this.risk = risk; }

        public String getConfidence() { return confidence; }
        public void setConfidence(String confidence) { this.confidence = confidence; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getSolution() { return solution; }
        public void setSolution(String solution) { this.solution = solution; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public String getParam() { return param; }
        public void setParam(String param) { this.param = param; }

        public String getCweId() { return cweId; }
        public void setCweId(String cweId) { this.cweId = cweId; }

        public List<String> getInstanceUrls() { return instanceUrls; }
        public void setInstanceUrls(List<String> instanceUrls) { this.instanceUrls = instanceUrls; }
    }
}
