package com.secgraph.controller;

import com.secgraph.model.Target;
import com.secgraph.repository.TargetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/targets")
public class TargetController {

    private final TargetRepository targetRepository;

    public TargetController(TargetRepository targetRepository) {
        this.targetRepository = targetRepository;
    }

    @GetMapping
    public ResponseEntity<List<Target>> getAll() {
        return ResponseEntity.ok(targetRepository.findAllTargets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Target> getById(@PathVariable Long id) {
        return targetRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/domain/{domain}")
    public ResponseEntity<Target> getByDomain(@PathVariable String domain) {
        return targetRepository.findByDomain(domain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        targetRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
