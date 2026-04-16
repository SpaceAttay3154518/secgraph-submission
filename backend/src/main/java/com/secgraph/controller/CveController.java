package com.secgraph.controller;

import com.secgraph.model.Cve;
import com.secgraph.repository.CveRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cves")
public class CveController {

    private final CveRepository cveRepository;

    public CveController(CveRepository cveRepository) {
        this.cveRepository = cveRepository;
    }

    @GetMapping
    public ResponseEntity<List<Cve>> getAll() {
        return ResponseEntity.ok(cveRepository.findAll());
    }

    @GetMapping("/target/{domain}")
    public ResponseEntity<List<Cve>> getByTarget(@PathVariable String domain) {
        return ResponseEntity.ok(cveRepository.findByTargetDomain(domain));
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<Cve>> getBySeverity(@PathVariable String severity) {
        return ResponseEntity.ok(cveRepository.findBySeverity(severity.toUpperCase()));
    }
}
