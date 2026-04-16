package com.secgraph.config;

import com.secgraph.model.*;
import com.secgraph.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Configuration
public class SeedDataLoader {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoader.class);

    @Bean
    @Profile("demo")
    public CommandLineRunner loadDemoData(TargetRepository targetRepo,
                                           EndpointRepository endpointRepo,
                                           TechnologyRepository techRepo,
                                           CveRepository cveRepo,
                                           VulnerabilityTypeRepository vulnTypeRepo,
                                           ScanJobRepository scanJobRepo) {
        return args -> {
            if (targetRepo.count() > 0) {
                log.info("Demo data already loaded, skipping");
                return;
            }

            log.info("Loading demo data...");

            VulnerabilityType xss = vulnTypeRepo.save(new VulnerabilityType("XSS", "A03:2021", "HIGH"));
            VulnerabilityType sqli = vulnTypeRepo.save(new VulnerabilityType("SQL Injection", "A03:2021", "CRITICAL"));
            VulnerabilityType csrf = vulnTypeRepo.save(new VulnerabilityType("CSRF", "A01:2021", "MEDIUM"));
            VulnerabilityType idor = vulnTypeRepo.save(new VulnerabilityType("IDOR", "A01:2021", "HIGH"));
            VulnerabilityType ssrf = vulnTypeRepo.save(new VulnerabilityType("SSRF", "A10:2021", "HIGH"));
            VulnerabilityType infoDisc = vulnTypeRepo.save(new VulnerabilityType("Information Disclosure", "A01:2021", "LOW"));

            Cve cve1 = new Cve("CVE-2024-21762", 9.8, "CRITICAL", "Fortinet FortiOS out-of-bound write vulnerability");
            cve1.setPublishedDate(LocalDate.of(2024, 2, 9));
            cve1.setExploits(List.of(sqli));
            cve1 = cveRepo.save(cve1);

            Cve cve2 = new Cve("CVE-2023-44487", 7.5, "HIGH", "HTTP/2 Rapid Reset attack allows DoS");
            cve2.setPublishedDate(LocalDate.of(2023, 10, 10));
            cve2 = cveRepo.save(cve2);

            Cve cve3 = new Cve("CVE-2024-3400", 10.0, "CRITICAL", "PAN-OS command injection in GlobalProtect");
            cve3.setPublishedDate(LocalDate.of(2024, 4, 12));
            cve3.setExploits(List.of(sqli));
            cve3 = cveRepo.save(cve3);

            Cve cve4 = new Cve("CVE-2023-46747", 9.8, "CRITICAL", "F5 BIG-IP authentication bypass");
            cve4.setPublishedDate(LocalDate.of(2023, 10, 26));
            cve4 = cveRepo.save(cve4);

            Cve cve5 = new Cve("CVE-2024-1709", 8.1, "HIGH", "ConnectWise ScreenConnect authentication bypass");
            cve5.setPublishedDate(LocalDate.of(2024, 2, 21));
            cve5 = cveRepo.save(cve5);

            Cve cve6 = new Cve("CVE-2023-34362", 9.8, "CRITICAL", "MOVEit Transfer SQL injection");
            cve6.setPublishedDate(LocalDate.of(2023, 6, 2));
            cve6.setExploits(List.of(sqli));
            cve6 = cveRepo.save(cve6);

            Technology nginx = new Technology("nginx", "1.18.0", "server");
            nginx.setConfidence(0.95);
            nginx.setCves(List.of(cve2));
            nginx = techRepo.save(nginx);

            Technology express = new Technology("Express.js", "4.17.1", "framework");
            express.setConfidence(0.85);
            express = techRepo.save(express);

            Technology react = new Technology("React", "18.2.0", "framework");
            react.setConfidence(0.9);
            react = techRepo.save(react);

            Technology node = new Technology("Node.js", "18.19.0", "language");
            node.setConfidence(0.8);
            node.setCves(List.of(cve5));
            node = techRepo.save(node);

            Technology postgres = new Technology("PostgreSQL", "15.4", "server");
            postgres.setConfidence(0.7);
            postgres.setCves(List.of(cve6));
            postgres = techRepo.save(postgres);

            Technology jquery = new Technology("jQuery", "3.6.0", "library");
            jquery.setConfidence(0.9);
            jquery = techRepo.save(jquery);

            Endpoint root = new Endpoint("/", "GET");
            root.setStatusCode(200);
            root.setContentType("text/html");
            root.setHeaders(List.of(
                    new Header("Strict-Transport-Security", "max-age=31536000"),
                    new Header("X-Frame-Options", "DENY"),
                    new Header("Content-Security-Policy", "MISSING")
            ));
            root = endpointRepo.save(root);

            Endpoint login = new Endpoint("/api/login", "POST");
            login.setStatusCode(200);
            login.setContentType("application/json");
            login.setParameters(List.of(
                    new Parameter("username", "string", "body"),
                    new Parameter("password", "string", "body")
            ));
            login.setVulnerabilities(List.of(sqli, csrf));
            login.setRiskScore(8.5);
            login = endpointRepo.save(login);

            Endpoint users = new Endpoint("/api/users", "GET");
            users.setStatusCode(200);
            users.setContentType("application/json");
            users.setParameters(List.of(new Parameter("id", "integer", "query")));
            users.setVulnerabilities(List.of(idor));
            users.setRiskScore(7.2);
            users = endpointRepo.save(users);

            Endpoint search = new Endpoint("/api/search", "GET");
            search.setStatusCode(200);
            search.setContentType("application/json");
            search.setParameters(List.of(
                    new Parameter("q", "string", "query"),
                    new Parameter("page", "integer", "query"),
                    new Parameter("limit", "integer", "query")
            ));
            search.setVulnerabilities(List.of(xss, sqli));
            search.setRiskScore(9.1);
            search = endpointRepo.save(search);

            Endpoint upload = new Endpoint("/api/upload", "POST");
            upload.setStatusCode(200);
            upload.setContentType("multipart/form-data");
            upload.setParameters(List.of(new Parameter("file", "file", "body")));
            upload.setVulnerabilities(List.of(ssrf));
            upload.setRiskScore(6.8);
            upload = endpointRepo.save(upload);

            Endpoint profile = new Endpoint("/api/profile", "GET");
            profile.setStatusCode(200);
            profile.setContentType("application/json");
            profile.setParameters(List.of(new Parameter("userId", "integer", "query")));
            profile.setVulnerabilities(List.of(idor, infoDisc));
            profile.setRiskScore(5.5);
            profile = endpointRepo.save(profile);

            Endpoint admin = new Endpoint("/admin/dashboard", "GET");
            admin.setStatusCode(200);
            admin.setContentType("text/html");
            admin.setRiskScore(4.0);
            admin = endpointRepo.save(admin);

            Endpoint apiDocs = new Endpoint("/api/docs", "GET");
            apiDocs.setStatusCode(200);
            apiDocs.setContentType("text/html");
            apiDocs.setVulnerabilities(List.of(infoDisc));
            apiDocs.setRiskScore(2.0);
            apiDocs = endpointRepo.save(apiDocs);

            Endpoint password = new Endpoint("/api/password/reset", "POST");
            password.setStatusCode(200);
            password.setContentType("application/json");
            password.setParameters(List.of(
                    new Parameter("email", "string", "body"),
                    new Parameter("token", "string", "body")
            ));
            password.setVulnerabilities(List.of(csrf));
            password.setRiskScore(6.0);
            password = endpointRepo.save(password);

            root.setFlowsTo(List.of(login, search));
            endpointRepo.save(root);
            login.setFlowsTo(List.of(profile, admin));
            endpointRepo.save(login);
            profile.setFlowsTo(List.of(upload));
            endpointRepo.save(profile);
            search.setFlowsTo(List.of(users));
            endpointRepo.save(search);
            admin.setFlowsTo(List.of(users));
            endpointRepo.save(admin);

            Target target = new Target("demo.secgraph.io");
            target.setIp("93.184.216.34");
            target.setEndpoints(List.of(root, login, users, search, upload, profile, admin, apiDocs, password));
            target.setTechnologies(List.of(nginx, express, react, node, postgres, jquery));

            ScanJob demoScan = new ScanJob("FULL");
            demoScan.setStatus("COMPLETED");
            demoScan.setCompletedAt(Instant.now());
            demoScan.setDiscoveredEndpoints(List.of(root, login, users, search, upload, profile, admin, apiDocs, password));
            demoScan = scanJobRepo.save(demoScan);

            target.setScanJobs(List.of(demoScan));
            targetRepo.save(target);

            log.info("Demo data loaded: 1 target, 9 endpoints, 6 technologies, 6 CVEs, 6 vulnerability types");
        };
    }
}
