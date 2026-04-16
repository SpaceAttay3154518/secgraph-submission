package com.secgraph.controller;

import com.secgraph.service.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/burp")
    public ResponseEntity<Map<String, Object>> importBurp(
            @RequestParam String domain,
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(importService.importBurp(domain, file.getInputStream()));
    }

    @PostMapping("/nmap")
    public ResponseEntity<Map<String, Object>> importNmap(
            @RequestParam String domain,
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(importService.importNmap(domain, file.getInputStream()));
    }

    @PostMapping("/zap")
    public ResponseEntity<Map<String, Object>> importZap(
            @RequestParam String domain,
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(importService.importZap(domain, file.getInputStream()));
    }
}
