package com.secgraph.service;

import com.secgraph.dto.GraphResponse;
import com.secgraph.dto.GraphResponse.GraphLink;
import com.secgraph.dto.GraphResponse.GraphNode;
import com.secgraph.model.*;
import com.secgraph.repository.TargetRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GraphExportService {

    private final TargetRepository targetRepository;

    public GraphExportService(TargetRepository targetRepository) {
        this.targetRepository = targetRepository;
    }

    public GraphResponse exportGraph(Long targetId) {
        Target target = targetRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Target not found: " + targetId));

        List<GraphNode> nodes = new ArrayList<>();
        List<GraphLink> links = new ArrayList<>();
        Set<String> addedNodes = new HashSet<>();

        String targetNodeId = "target-" + target.getId();
        nodes.add(new GraphNode(targetNodeId, target.getDomain(), "target", null));
        addedNodes.add(targetNodeId);

        for (Endpoint ep : target.getEndpoints()) {
            String epId = "endpoint-" + ep.getId();
            if (addedNodes.add(epId)) {
                nodes.add(new GraphNode(epId, ep.getMethod() + " " + ep.getPath(), "endpoint", ep.getRiskScore()));
                links.add(new GraphLink(targetNodeId, epId, "HAS_ENDPOINT"));

                for (Parameter param : ep.getParameters()) {
                    String paramId = "param-" + param.getId();
                    if (addedNodes.add(paramId)) {
                        nodes.add(new GraphNode(paramId, param.getName() + " (" + param.getLocation() + ")", "parameter", null));
                    }
                    links.add(new GraphLink(epId, paramId, "ACCEPTS"));
                }

                for (VulnerabilityType vuln : ep.getVulnerabilities()) {
                    String vulnId = "vuln-" + vuln.getId();
                    if (addedNodes.add(vulnId)) {
                        nodes.add(new GraphNode(vulnId, vuln.getName(), "vulnerability",
                                "CRITICAL".equals(vuln.getRiskLevel()) ? 10.0 :
                                "HIGH".equals(vuln.getRiskLevel()) ? 7.0 :
                                "MEDIUM".equals(vuln.getRiskLevel()) ? 4.0 : 1.0));
                    }
                    links.add(new GraphLink(epId, vulnId, "POTENTIALLY_VULNERABLE_TO"));
                }

                for (Endpoint flowTarget : ep.getFlowsTo()) {
                    String flowId = "endpoint-" + flowTarget.getId();
                    links.add(new GraphLink(epId, flowId, "FLOWS_TO"));
                }
            }
        }

        for (Technology tech : target.getTechnologies()) {
            String techId = "tech-" + tech.getId();
            if (addedNodes.add(techId)) {
                String label = tech.getVersion() != null ? tech.getName() + " " + tech.getVersion() : tech.getName();
                nodes.add(new GraphNode(techId, label, "technology", null));
                links.add(new GraphLink(targetNodeId, techId, "USES"));

                for (Cve cve : tech.getCves()) {
                    String cveId = "cve-" + cve.getId();
                    if (addedNodes.add(cveId)) {
                        nodes.add(new GraphNode(cveId, cve.getCveId(), "cve", cve.getCvssScore()));
                    }
                    links.add(new GraphLink(techId, cveId, "AFFECTED_BY"));
                }
            }
        }

        return new GraphResponse(nodes, links);
    }
}
