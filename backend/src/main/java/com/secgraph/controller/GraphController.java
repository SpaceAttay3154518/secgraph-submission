package com.secgraph.controller;

import com.secgraph.dto.GraphResponse;
import com.secgraph.service.GraphExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph")
public class GraphController {

    private final GraphExportService graphExportService;

    public GraphController(GraphExportService graphExportService) {
        this.graphExportService = graphExportService;
    }

    @GetMapping("/{targetId}")
    public ResponseEntity<GraphResponse> getGraph(@PathVariable Long targetId) {
        return ResponseEntity.ok(graphExportService.exportGraph(targetId));
    }
}
