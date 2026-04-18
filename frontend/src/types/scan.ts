export interface ScanRequest {
  url: string;
  depth: number;
  scanType: "FULL" | "CRAWL_ONLY" | "FINGERPRINT_ONLY";
}

export interface ScanJob {
  id: number;
  status: string;
  startedAt: string;
  completedAt: string | null;
  scanType: string;
  errorMessage: string | null;
}

export interface Target {
  id: number;
  domain: string;
  ip: string | null;
  firstSeen: string;
  lastSeen: string;
}
