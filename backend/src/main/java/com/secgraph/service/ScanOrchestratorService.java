package com.secgraph.service;

import com.secgraph.dto.ReconResult;
import com.secgraph.model.*;
import com.secgraph.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Service
public class ScanOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(ScanOrchestratorService.class);

    private final ReconClientService reconClient;
    private final CveCorrelationService cveCorrelationService;
    private final TargetRepository targetRepository;
    private final EndpointRepository endpointRepository;
    private final TechnologyRepository technologyRepository;
    private final ScanJobRepository scanJobRepository;

    public ScanOrchestratorService(ReconClientService reconClient,
                                    CveCorrelationService cveCorrelationService,
                                    TargetRepository targetRepository,
                                    EndpointRepository endpointRepository,
                                    TechnologyRepository technologyRepository,
                                    ScanJobRepository scanJobRepository) {
        this.reconClient = reconClient;
        this.cveCorrelationService = cveCorrelationService;
        this.targetRepository = targetRepository;
        this.endpointRepository = endpointRepository;
        this.technologyRepository = technologyRepository;
        this.scanJobRepository = scanJobRepository;
    }

    // Creates a scan job and kicks off the async scan pipeline
    public ScanJob startScan(String url, int depth, String scanType) {
        String domain = extractDomain(url);

        Target target = targetRepository.findByDomain(domain)
                .orElseGet(() -> targetRepository.save(new Target(domain)));
        target.setLastSeen(Instant.now());
        targetRepository.save(target);

        ScanJob scanJob = new ScanJob(scanType);
        scanJob = scanJobRepository.save(scanJob);
        target.getScanJobs().add(scanJob);
        targetRepository.save(target);

        executeScan(target, scanJob, url, depth, scanType);

        return scanJob;
    }

    // Runs each scan phase in order: crawl, fingerprint, headers, CVE lookup
    @Async("scanExecutor")
    public void executeScan(Target target, ScanJob scanJob, String url, int depth, String scanType) {
        try {
            if ("FULL".equals(scanType) || "CRAWL_ONLY".equals(scanType)) {
                updateScanStatus(scanJob, "CRAWLING");
                List<ReconResult.DiscoveredEndpoint> endpoints = reconClient.crawl(url, depth);
                storeEndpoints(target, scanJob, endpoints);
                log.info("Crawl complete: {} endpoints discovered", endpoints.size());
            }

            if ("FULL".equals(scanType) || "FINGERPRINT_ONLY".equals(scanType)) {
                updateScanStatus(scanJob, "FINGERPRINTING");
                List<ReconResult.DiscoveredTechnology> techs = reconClient.fingerprint(url);
                storeTechnologies(target, techs);
                log.info("Fingerprint complete: {} technologies detected", techs.size());
            }

            if ("FULL".equals(scanType)) {
                updateScanStatus(scanJob, "ANALYZING_HEADERS");
                ReconResult.HeaderAnalysis headers = reconClient.analyzeHeaders(url);
                if (headers != null) {
                    storeHeaders(target, headers);
                    log.info("Header analysis complete, score: {}", headers.getScore());
                }
            }

            if ("FULL".equals(scanType)) {
                updateScanStatus(scanJob, "CVE_LOOKUP");
                Target refreshed = targetRepository.findById(target.getId()).orElse(target);
                cveCorrelationService.correlateAll(refreshed.getTechnologies());
                log.info("CVE correlation complete");
            }

            scanJob.setStatus("COMPLETED");
            scanJob.setCompletedAt(Instant.now());
            scanJobRepository.save(scanJob);

            log.info("Scan completed for {}", url);

        } catch (Exception e) {
            log.error("Scan failed for {}: {}", url, e.getMessage(), e);
            scanJob.setStatus("FAILED");
            scanJob.setErrorMessage(e.getMessage());
            scanJob.setCompletedAt(Instant.now());
            scanJobRepository.save(scanJob);
        }
    }

    private void storeEndpoints(Target target, ScanJob scanJob,
                                 List<ReconResult.DiscoveredEndpoint> discovered) {
        for (ReconResult.DiscoveredEndpoint ep : discovered) {
            List<Endpoint> existing = endpointRepository
                    .findByTargetIdAndPathAndMethod(target.getId(), ep.getPath(), ep.getMethod());
            if (!existing.isEmpty()) continue;

            Endpoint endpoint = new Endpoint(ep.getPath(), ep.getMethod());
            endpoint.setStatusCode(ep.getStatusCode());
            endpoint.setContentType(ep.getContentType());

            if (ep.getParams() != null) {
                for (ReconResult.DiscoveredParam p : ep.getParams()) {
                    endpoint.getParameters().add(new Parameter(p.getName(), p.getType(), p.getLocation()));
                }
            }

            endpoint = endpointRepository.save(endpoint);
            target.getEndpoints().add(endpoint);
            scanJob.getDiscoveredEndpoints().add(endpoint);
        }

        targetRepository.save(target);
        scanJobRepository.save(scanJob);
    }

    private void storeTechnologies(Target target, List<ReconResult.DiscoveredTechnology> discovered) {
        for (ReconResult.DiscoveredTechnology tech : discovered) {
            Technology technology = technologyRepository
                    .findByNameAndVersion(tech.getName(), tech.getVersion())
                    .orElseGet(() -> {
                        Technology t = new Technology(tech.getName(), tech.getVersion(), tech.getCategory());
                        t.setConfidence(tech.getConfidence());
                        return technologyRepository.save(t);
                    });

            if (!target.getTechnologies().contains(technology)) {
                target.getTechnologies().add(technology);
            }
        }
        targetRepository.save(target);
    }

    // Attaches discovered headers to the root endpoint of the target
    private void storeHeaders(Target target, ReconResult.HeaderAnalysis analysis) {
        if (analysis.getHeaders() == null || target.getEndpoints().isEmpty()) return;

        Endpoint root = target.getEndpoints().stream()
                .filter(e -> "/".equals(e.getPath()) || "".equals(e.getPath()))
                .findFirst()
                .orElse(target.getEndpoints().get(0));

        analysis.getHeaders().forEach((name, value) -> {
            root.getHeaders().add(new Header(name, value != null ? value : "MISSING"));
        });
        endpointRepository.save(root);
    }

    private void updateScanStatus(ScanJob scanJob, String status) {
        scanJob.setStatus(status);
        scanJobRepository.save(scanJob);
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (Exception e) {
            return url.replaceAll("https?://", "").split("/")[0];
        }
    }
}
