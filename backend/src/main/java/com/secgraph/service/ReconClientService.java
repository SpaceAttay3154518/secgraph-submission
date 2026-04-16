package com.secgraph.service;

import com.secgraph.dto.ReconResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ReconClientService {

    private static final Logger log = LoggerFactory.getLogger(ReconClientService.class);
    private final WebClient reconClient;

    public ReconClientService(@Qualifier("reconWebClient") WebClient reconClient) {
        this.reconClient = reconClient;
    }

    public List<ReconResult.DiscoveredEndpoint> crawl(String url, int depth) {
        log.info("Starting crawl of {} with depth {}", url, depth);

        CrawlResponse response = reconClient.post()
                .uri("/recon/crawl")
                .bodyValue(Map.of("url", url, "depth", depth, "timeout", 30))
                .retrieve()
                .bodyToMono(CrawlResponse.class)
                .timeout(Duration.ofMinutes(5))
                .block();

        return response != null ? response.endpoints : List.of();
    }

    public List<ReconResult.DiscoveredTechnology> fingerprint(String url) {
        log.info("Starting fingerprint of {}", url);

        FingerprintResponse response = reconClient.post()
                .uri("/recon/fingerprint")
                .bodyValue(Map.of("url", url))
                .retrieve()
                .bodyToMono(FingerprintResponse.class)
                .timeout(Duration.ofMinutes(2))
                .block();

        return response != null ? response.technologies : List.of();
    }

    public ReconResult.HeaderAnalysis analyzeHeaders(String url) {
        log.info("Starting header analysis of {}", url);

        return reconClient.post()
                .uri("/recon/headers")
                .bodyValue(Map.of("url", url))
                .retrieve()
                .bodyToMono(ReconResult.HeaderAnalysis.class)
                .timeout(Duration.ofMinutes(1))
                .block();
    }

    public boolean isHealthy() {
        try {
            reconClient.get()
                    .uri("/recon/health")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static class CrawlResponse {
        public List<ReconResult.DiscoveredEndpoint> endpoints;
    }

    private static class FingerprintResponse {
        public List<ReconResult.DiscoveredTechnology> technologies;
    }
}
