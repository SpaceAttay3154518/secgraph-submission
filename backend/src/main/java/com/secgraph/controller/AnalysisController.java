package com.secgraph.controller;

import com.secgraph.algorithm.AttackPathFinder;
import com.secgraph.algorithm.AttackSurfaceScorer;
import com.secgraph.algorithm.ImpactAnalyzer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AttackPathFinder attackPathFinder;
    private final ImpactAnalyzer impactAnalyzer;
    private final AttackSurfaceScorer surfaceScorer;

    public AnalysisController(AttackPathFinder attackPathFinder,
                               ImpactAnalyzer impactAnalyzer,
                               AttackSurfaceScorer surfaceScorer) {
        this.attackPathFinder = attackPathFinder;
        this.impactAnalyzer = impactAnalyzer;
        this.surfaceScorer = surfaceScorer;
    }

    @GetMapping("/attack-paths/{targetId}")
    public ResponseEntity<List<AttackPathFinder.AttackPath>> getAttackPaths(
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "5.0") double minCvss,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(attackPathFinder.findAttackPaths(targetId, minCvss, limit));
    }

    @GetMapping("/blast-radius/{endpointId}")
    public ResponseEntity<AttackPathFinder.BlastRadius> getBlastRadius(
            @PathVariable Long endpointId,
            @RequestParam(defaultValue = "5") int maxDepth) {
        return ResponseEntity.ok(attackPathFinder.findBlastRadius(endpointId, maxDepth));
    }

    @GetMapping("/centrality/{targetId}")
    public ResponseEntity<List<ImpactAnalyzer.EndpointImpact>> getCentrality(
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(impactAnalyzer.computeCentrality(targetId, limit));
    }

    @GetMapping("/data-flow/{targetId}")
    public ResponseEntity<List<ImpactAnalyzer.FlowAnalysis>> getDataFlow(
            @PathVariable Long targetId) {
        return ResponseEntity.ok(impactAnalyzer.analyzeDataFlow(targetId));
    }

    @GetMapping("/scores/{targetId}")
    public ResponseEntity<List<AttackSurfaceScorer.EndpointScore>> getScores(
            @PathVariable Long targetId) {
        return ResponseEntity.ok(surfaceScorer.scoreEndpoints(targetId));
    }
}
