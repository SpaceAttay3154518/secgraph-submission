package com.secgraph.dto;

import jakarta.validation.constraints.NotBlank;

public class ScanRequest {

    @NotBlank(message = "Target URL is required")
    private String url;

    private int depth = 2;
    private String scanType = "FULL";

    public ScanRequest() {}

    public ScanRequest(String url) {
        this.url = url;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }

    public String getScanType() { return scanType; }
    public void setScanType(String scanType) { this.scanType = scanType; }
}
