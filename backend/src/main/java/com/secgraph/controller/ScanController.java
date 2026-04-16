package com.secgraph.controller;

import com.secgraph.dto.ScanRequest;
import com.secgraph.model.ScanJob;
import com.secgraph.repository.ScanJobRepository;
import com.secgraph.service.ScanOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scans")
public class ScanController {

    private final ScanOrchestratorService scanOrchestrator;
    private final ScanJobRepository scanJobRepository;

    public ScanController(ScanOrchestratorService scanOrchestrator,
                          ScanJobRepository scanJobRepository) {
        this.scanOrchestrator = scanOrchestrator;
        this.scanJobRepository = scanJobRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> startScan(@Valid @RequestBody ScanRequest request) {
        ScanJob job = scanOrchestrator.startScan(request.getUrl(), request.getDepth(), request.getScanType());
        return ResponseEntity.ok(Map.of(
                "scanId", job.getId(),
                "status", job.getStatus(),
                "message", "Scan started"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScanJob> getScan(@PathVariable Long id) {
        return scanJobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ScanJob>> getAllScans() {
        return ResponseEntity.ok(scanJobRepository.findAll());
    }
}
