package com.secgraph.service;

import com.secgraph.dto.ReconResult.DiscoveredEndpoint;
import com.secgraph.dto.ReconResult.DiscoveredTechnology;
import com.secgraph.model.*;
import com.secgraph.parser.BurpXmlParser;
import com.secgraph.parser.NmapXmlParser;
import com.secgraph.parser.ZapJsonParser;
import com.secgraph.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private final TargetRepository targetRepository;
    private final EndpointRepository endpointRepository;
    private final TechnologyRepository technologyRepository;
    private final VulnerabilityTypeRepository vulnTypeRepository;

    public ImportService(TargetRepository targetRepository,
                         EndpointRepository endpointRepository,
                         TechnologyRepository technologyRepository,
                         VulnerabilityTypeRepository vulnTypeRepository) {
        this.targetRepository = targetRepository;
        this.endpointRepository = endpointRepository;
        this.technologyRepository = technologyRepository;
        this.vulnTypeRepository = vulnTypeRepository;
    }

    public Map<String, Object> importBurp(String domain, InputStream xml) throws Exception {
        Target target = getOrCreateTarget(domain);
        List<DiscoveredEndpoint> endpoints = BurpXmlParser.parse(xml);

        int imported = 0;
        for (DiscoveredEndpoint ep : endpoints) {
            List<Endpoint> existing = endpointRepository
                    .findByTargetIdAndPathAndMethod(target.getId(), ep.getPath(), ep.getMethod());
            if (!existing.isEmpty()) continue;

            Endpoint endpoint = new Endpoint(ep.getPath(), ep.getMethod());
            endpoint.setStatusCode(ep.getStatusCode());
            endpoint.setContentType(ep.getContentType());

            if (ep.getParams() != null) {
                for (var p : ep.getParams()) {
                    endpoint.getParameters().add(new Parameter(p.getName(), p.getType(), p.getLocation()));
                }
            }

            endpoint = endpointRepository.save(endpoint);
            target.getEndpoints().add(endpoint);
            imported++;
        }
        targetRepository.save(target);

        log.info("Burp import: {} endpoints for {}", imported, domain);
        return Map.of("source", "burp", "endpointsImported", imported, "totalParsed", endpoints.size());
    }

    public Map<String, Object> importNmap(String domain, InputStream xml) throws Exception {
        Target target = getOrCreateTarget(domain);
        NmapXmlParser.ParseResult result = NmapXmlParser.parse(xml);

        int imported = 0;
        for (DiscoveredTechnology tech : result.getTechnologies()) {
            Technology existing = technologyRepository
                    .findByNameAndVersion(tech.getName(), tech.getVersion()).orElse(null);
            if (existing == null) {
                Technology t = new Technology(tech.getName(), tech.getVersion(), tech.getCategory());
                t.setConfidence(tech.getConfidence());
                existing = technologyRepository.save(t);
                imported++;
            }
            if (!target.getTechnologies().contains(existing)) {
                target.getTechnologies().add(existing);
            }
        }
        targetRepository.save(target);

        log.info("Nmap import: {} technologies for {}", imported, domain);
        return Map.of("source", "nmap", "technologiesImported", imported,
                "openPorts", result.getOpenPorts().size());
    }

    public Map<String, Object> importZap(String domain, InputStream json) throws Exception {
        Target target = getOrCreateTarget(domain);
        List<ZapJsonParser.ZapAlert> alerts = ZapJsonParser.parse(json);

        int endpointsImported = 0;
        int vulnsLinked = 0;

        for (ZapJsonParser.ZapAlert alert : alerts) {
            String path = alert.getPath() != null ? alert.getPath() : "/";

            List<Endpoint> existing = endpointRepository
                    .findByTargetIdAndPathAndMethod(target.getId(), path, "GET");
            Endpoint endpoint;
            if (!existing.isEmpty()) {
                endpoint = existing.get(0);
            } else {
                endpoint = new Endpoint(path, "GET");
                endpoint = endpointRepository.save(endpoint);
                target.getEndpoints().add(endpoint);
                endpointsImported++;
            }

            if (alert.getAlert() != null) {
                String vulnName = alert.getAlert();
                VulnerabilityType vt = vulnTypeRepository.findByName(vulnName)
                        .orElseGet(() -> {
                            VulnerabilityType newVt = new VulnerabilityType();
                            newVt.setName(vulnName);
                            newVt.setDescription(alert.getDescription());
                            newVt.setRiskLevel(mapZapRisk(alert.getRisk()));
                            return vulnTypeRepository.save(newVt);
                        });

                if (!endpoint.getVulnerabilities().contains(vt)) {
                    endpoint.getVulnerabilities().add(vt);
                    vulnsLinked++;
                }
            }

            if (alert.getParam() != null && !alert.getParam().isBlank()) {
                boolean paramExists = endpoint.getParameters().stream()
                        .anyMatch(p -> alert.getParam().equals(p.getName()));
                if (!paramExists) {
                    endpoint.getParameters().add(new Parameter(alert.getParam(), "string", "query"));
                }
            }

            endpointRepository.save(endpoint);
        }
        targetRepository.save(target);

        log.info("ZAP import: {} endpoints, {} vulns for {}", endpointsImported, vulnsLinked, domain);
        return Map.of("source", "zap", "endpointsImported", endpointsImported,
                "vulnerabilitiesLinked", vulnsLinked, "totalAlerts", alerts.size());
    }

    private Target getOrCreateTarget(String domain) {
        return targetRepository.findByDomain(domain)
                .orElseGet(() -> {
                    Target t = new Target(domain);
                    return targetRepository.save(t);
                });
    }

    private String mapZapRisk(String risk) {
        if (risk == null) return "LOW";
        return switch (risk.toLowerCase()) {
            case "high" -> "HIGH";
            case "medium" -> "MEDIUM";
            case "low" -> "LOW";
            case "informational" -> "LOW";
            default -> "MEDIUM";
        };
    }
}
