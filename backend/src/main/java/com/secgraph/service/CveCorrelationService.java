package com.secgraph.service;

import com.secgraph.dto.NvdResponse;
import com.secgraph.model.Cve;
import com.secgraph.model.Technology;
import com.secgraph.model.VulnerabilityType;
import com.secgraph.repository.CveRepository;
import com.secgraph.repository.TechnologyRepository;
import com.secgraph.repository.VulnerabilityTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
public class CveCorrelationService {

    private static final Logger log = LoggerFactory.getLogger(CveCorrelationService.class);

    private final WebClient nvdClient;
    private final CveRepository cveRepository;
    private final TechnologyRepository technologyRepository;
    private final VulnerabilityTypeRepository vulnTypeRepository;
    private final String apiKey;

    private static final Map<String, String> CWE_TO_VULN = Map.ofEntries(
            Map.entry("CWE-79", "XSS"),
            Map.entry("CWE-89", "SQL Injection"),
            Map.entry("CWE-22", "Path Traversal"),
            Map.entry("CWE-78", "OS Command Injection"),
            Map.entry("CWE-352", "CSRF"),
            Map.entry("CWE-287", "Authentication Bypass"),
            Map.entry("CWE-306", "Missing Authentication"),
            Map.entry("CWE-862", "Missing Authorization"),
            Map.entry("CWE-918", "SSRF"),
            Map.entry("CWE-502", "Insecure Deserialization"),
            Map.entry("CWE-611", "XXE"),
            Map.entry("CWE-94", "Code Injection"),
            Map.entry("CWE-434", "Unrestricted File Upload"),
            Map.entry("CWE-200", "Information Disclosure"),
            Map.entry("CWE-522", "Weak Credentials"),
            Map.entry("CWE-269", "Privilege Escalation"),
            Map.entry("CWE-601", "Open Redirect"),
            Map.entry("CWE-776", "XML Entity Expansion"),
            Map.entry("CWE-400", "Resource Exhaustion"),
            Map.entry("CWE-120", "Buffer Overflow")
    );

    private static final Map<String, String> CWE_TO_OWASP = Map.ofEntries(
            Map.entry("CWE-79", "A03:2021"),
            Map.entry("CWE-89", "A03:2021"),
            Map.entry("CWE-22", "A01:2021"),
            Map.entry("CWE-78", "A03:2021"),
            Map.entry("CWE-352", "A01:2021"),
            Map.entry("CWE-287", "A07:2021"),
            Map.entry("CWE-918", "A10:2021"),
            Map.entry("CWE-502", "A08:2021"),
            Map.entry("CWE-611", "A05:2021"),
            Map.entry("CWE-200", "A01:2021"),
            Map.entry("CWE-434", "A04:2021")
    );

    public CveCorrelationService(@Qualifier("nvdWebClient") WebClient nvdClient,
                                  CveRepository cveRepository,
                                  TechnologyRepository technologyRepository,
                                  VulnerabilityTypeRepository vulnTypeRepository,
                                  @Value("${nvd.api.key:}") String apiKey) {
        this.nvdClient = nvdClient;
        this.cveRepository = cveRepository;
        this.technologyRepository = technologyRepository;
        this.vulnTypeRepository = vulnTypeRepository;
        this.apiKey = apiKey;
    }

    public List<Cve> correlate(Technology technology) {
        if (technology.getName() == null) return List.of();

        if (!technology.getCves().isEmpty()) {
            return technology.getCves();
        }

        String keyword = technology.getName();
        if (technology.getVersion() != null && !technology.getVersion().isBlank()) {
            keyword += " " + technology.getVersion();
        }

        log.info("Looking up CVEs for: {}", keyword);

        try {
            NvdResponse response = fetchFromNvd(keyword);
            if (response == null || response.getVulnerabilities() == null) {
                return List.of();
            }

            List<Cve> cves = new ArrayList<>();
            int limit = Math.min(response.getVulnerabilities().size(), 20);

            for (int i = 0; i < limit; i++) {
                NvdResponse.Vulnerability vuln = response.getVulnerabilities().get(i);
                NvdResponse.CveItem item = vuln.getCve();

                Cve cve = cveRepository.findByCveId(item.getId()).orElse(null);
                if (cve == null) {
                    cve = new Cve();
                    cve.setCveId(item.getId());
                    cve.setDescription(extractDescription(item));
                    cve.setPublishedDate(parseDate(item.getPublished()));

                    if (item.getMetrics() != null && item.getMetrics().getCvssMetricV31() != null
                            && !item.getMetrics().getCvssMetricV31().isEmpty()) {
                        NvdResponse.CvssData cvss = item.getMetrics().getCvssMetricV31().get(0).getCvssData();
                        cve.setCvssScore(cvss.getBaseScore());
                        cve.setSeverity(cvss.getBaseSeverity());
                    } else {
                        cve.setCvssScore(0.0);
                        cve.setSeverity("UNKNOWN");
                    }

                    List<VulnerabilityType> exploits = mapCweToVulnTypes(item);
                    cve.setExploits(exploits);

                    cve = cveRepository.save(cve);
                }

                cves.add(cve);
            }

            technology.setCves(cves);
            technologyRepository.save(technology);

            log.info("Found {} CVEs for {}", cves.size(), keyword);
            return cves;

        } catch (Exception e) {
            log.error("Failed to look up CVEs for {}: {}", keyword, e.getMessage());
            return List.of();
        }
    }

    public void correlateAll(List<Technology> technologies) {
        for (Technology tech : technologies) {
            correlate(tech);
            try {
                Thread.sleep(apiKey.isBlank() ? 6500 : 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private NvdResponse fetchFromNvd(String keyword) {
        WebClient.RequestHeadersSpec<?> request = nvdClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("keywordSearch", keyword)
                        .queryParam("resultsPerPage", 20)
                        .build());

        if (!apiKey.isBlank()) {
            request = nvdClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("keywordSearch", keyword)
                            .queryParam("resultsPerPage", 20)
                            .build())
                    .header("apiKey", apiKey);
        }

        return request
                .retrieve()
                .bodyToMono(NvdResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();
    }

    private String extractDescription(NvdResponse.CveItem item) {
        if (item.getDescriptions() == null) return "";
        return item.getDescriptions().stream()
                .filter(d -> "en".equals(d.getLang()))
                .map(NvdResponse.Description::getValue)
                .findFirst()
                .orElse("");
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr.substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    private List<VulnerabilityType> mapCweToVulnTypes(NvdResponse.CveItem item) {
        List<VulnerabilityType> types = new ArrayList<>();
        if (item.getWeaknesses() == null) return types;

        for (NvdResponse.Weakness weakness : item.getWeaknesses()) {
            if (weakness.getDescription() == null) continue;
            for (NvdResponse.WeaknessDescription desc : weakness.getDescription()) {
                String cweId = desc.getValue();
                String vulnName = CWE_TO_VULN.get(cweId);
                if (vulnName != null) {
                    VulnerabilityType vt = vulnTypeRepository.findByName(vulnName)
                            .orElseGet(() -> {
                                VulnerabilityType newVt = new VulnerabilityType();
                                newVt.setName(vulnName);
                                newVt.setOwaspCategory(CWE_TO_OWASP.getOrDefault(cweId, ""));
                                newVt.setRiskLevel(inferRiskLevel(vulnName));
                                return vulnTypeRepository.save(newVt);
                            });
                    types.add(vt);
                }
            }
        }
        return types;
    }

    private String inferRiskLevel(String vulnName) {
        return switch (vulnName) {
            case "SQL Injection", "OS Command Injection", "Code Injection",
                 "Insecure Deserialization", "Buffer Overflow" -> "CRITICAL";
            case "XSS", "SSRF", "XXE", "Path Traversal", "Authentication Bypass",
                 "Unrestricted File Upload", "Privilege Escalation" -> "HIGH";
            case "CSRF", "Missing Authentication", "Missing Authorization",
                 "Weak Credentials" -> "MEDIUM";
            default -> "LOW";
        };
    }
}
