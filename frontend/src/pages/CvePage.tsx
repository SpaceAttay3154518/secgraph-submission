import { useEffect, useState } from "react";
import { getCves } from "../api/client";
import type { Cve } from "../types/cve";
import { getSeverityColor } from "../utils/severityColors";

export default function CvePage() {
  const [cves, setCves] = useState<Cve[]>([]);
  const [filter, setFilter] = useState("");

  useEffect(() => {
    getCves().then(setCves).catch(() => {});
  }, []);

  const filtered = cves.filter(
    (c) =>
      c.cveId.toLowerCase().includes(filter.toLowerCase()) ||
      c.description?.toLowerCase().includes(filter.toLowerCase())
  );

  return (
    <section className="page">
      <div className="page-header">
        <h1 className="page-title">CVEs</h1>
      </div>

      <input
        type="text"
        placeholder="Search CVEs..."
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
        className="input"
        style={{ marginBottom: 16, background: "var(--surface)" }}
      />

      {filtered.length === 0 ? (
        <p className="muted">
          {cves.length === 0 ? "No CVEs found. Run a scan to discover vulnerabilities." : "No matching CVEs."}
        </p>
      ) : (
        <div className="cve-list">
          {filtered.map((cve) => (
            <div
              key={cve.id}
              className="cve-card"
              style={{ borderLeftColor: getSeverityColor(cve.severity) }}
            >
              <div className="cve-card__header">
                <span className="cve-card__id">{cve.cveId}</span>
                <div className="cve-card__meta">
                  <span
                    className="severity-badge"
                    style={{
                    background: getSeverityColor(cve.severity) + "22",
                    color: getSeverityColor(cve.severity),
                    }}
                  >
                    {cve.severity}
                  </span>
                  <span style={{ color: "#94a3b8", fontSize: 13 }}>
                    CVSS {cve.cvssScore?.toFixed(1)}
                  </span>
                </div>
              </div>
              {cve.description && (
                <p className="cve-card__description">
                  {cve.description.slice(0, 200)}
                  {cve.description.length > 200 ? "..." : ""}
                </p>
              )}
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
