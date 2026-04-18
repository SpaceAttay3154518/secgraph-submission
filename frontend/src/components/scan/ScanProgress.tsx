import { useEffect, useState } from "react";
import { getScan } from "../../api/client";
import type { ScanJob } from "../../types/scan";

interface Props {
  scanId: number;
  onComplete: () => void;
}

const STATUS_LABELS: Record<string, string> = {
  PENDING: "Queued...",
  CRAWLING: "Crawling endpoints...",
  FINGERPRINTING: "Detecting technologies...",
  ANALYZING_HEADERS: "Analyzing security headers...",
  CVE_LOOKUP: "Looking up CVEs...",
  COMPLETED: "Scan complete",
  FAILED: "Scan failed",
};

export default function ScanProgress({ scanId, onComplete }: Props) {
  const [scan, setScan] = useState<ScanJob | null>(null);

  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const data = await getScan(scanId);
        setScan(data);
        if (data.status === "COMPLETED" || data.status === "FAILED") {
          clearInterval(interval);
          if (data.status === "COMPLETED") onComplete();
        }
      } catch {
        clearInterval(interval);
      }
    }, 2000);

    return () => clearInterval(interval);
  }, [scanId, onComplete]);

  if (!scan) return null;

  const isRunning = !["COMPLETED", "FAILED"].includes(scan.status);
  const statusLabel = STATUS_LABELS[scan.status] || scan.status;

  return (
    <div className={`status-panel${scan.status === "FAILED" ? " status-panel--failed" : ""}`}>
      <div className="status-line">
        {isRunning && (
          <div className="status-dot" />
        )}
        <span>{statusLabel}</span>
      </div>

      {scan.errorMessage && (
        <div className="error-text" style={{ marginTop: 8 }}>
          {scan.errorMessage}
        </div>
      )}
    </div>
  );
}
