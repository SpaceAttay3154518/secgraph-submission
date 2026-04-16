package com.secgraph.dto;

import java.util.List;

public class GraphResponse {

    private List<GraphNode> nodes;
    private List<GraphLink> links;

    public GraphResponse() {}

    public GraphResponse(List<GraphNode> nodes, List<GraphLink> links) {
        this.nodes = nodes;
        this.links = links;
    }

    public List<GraphNode> getNodes() { return nodes; }
    public void setNodes(List<GraphNode> nodes) { this.nodes = nodes; }

    public List<GraphLink> getLinks() { return links; }
    public void setLinks(List<GraphLink> links) { this.links = links; }

    public static class GraphNode {
        private String id;
        private String label;
        private String type;
        private String group;
        private Double score;
        private Object data;

        public GraphNode() {}

        public GraphNode(String id, String label, String type, Double score) {
            this.id = id;
            this.label = label;
            this.type = type;
            this.group = type;
            this.score = score;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }

        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }

        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    public static class GraphLink {
        private String source;
        private String target;
        private String type;
        private String label;

        public GraphLink() {}

        public GraphLink(String source, String target, String type) {
            this.source = source;
            this.target = target;
            this.type = type;
            this.label = type;
        }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
}
