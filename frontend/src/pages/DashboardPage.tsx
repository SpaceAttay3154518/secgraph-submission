import { useEffect, useState } from "react";
import { getTargets, getAllScans, getStats } from "../api/client";
import type { Target, ScanJob } from "../types/scan";
import type { TargetStats } from "../api/client";
import { Link } from "react-router-dom";
import SeverityChart from "../components/dashboard/SeverityChart";

export default function DashboardPage() {
  const [targets, setTargets] = useState<Target[]>([]);
  const [scans, setScans] = useState<ScanJob[]>([]);
  const [selectedTargetId, setSelectedTargetId] = useState<number | null>(null);
  const [stats, setStats] = useState<TargetStats | null>(null);

  useEffect(() => {
    getTargets().then((data) => {
      setTargets(data);
      if (data.length > 0) setSelectedTargetId(data[0].id);
    }).catch(() => {});
    getAllScans().then(setScans).catch(() => {});
  }, []);

  useEffect(() => {
    if (selectedTargetId == null) return;
    getStats(selectedTargetId).then(setStats).catch(() => setStats(null));
  }, [selectedTargetId]);

  return (
    <section className="page">
      <div className="page-header">
        <h1 className="page-title">Dashboard</h1>
        {targets.length > 0 && (
          <select
            value={selectedTargetId ?? ""}
            onChange={(e) => setSelectedTargetId(Number(e.target.value))}
            className="select select--compact"
          >
            {targets.map((t) => (
              <option key={t.id} value={t.id}>{t.domain}</option>
            ))}
          </select>
        )}
      </div>

      <div className="metric-grid">
        <StatCard label="Endpoints" value={stats?.endpoints ?? 0} color="#3b82f6" />
        <StatCard label="Technologies" value={stats?.technologies ?? 0} color="#22c55e" />
        <StatCard label="CVEs" value={stats?.cves ?? 0} color="#ef4444" />
        <StatCard label="Parameters" value={stats?.parameters ?? 0} color="#a855f7" />
      </div>

      <div className="responsive-grid mb-24">
        <article className="card">
          <h3 className="card-title">CVE Severity Distribution</h3>
          {stats?.severityBreakdown ? (
            <SeverityChart data={stats.severityBreakdown} />
          ) : (
            <p className="muted">No CVE data yet.</p>
          )}
        </article>

        <article className="card">
          <h3 className="card-title">Top Risk Endpoints</h3>
          {stats?.topRiskEndpoints && stats.topRiskEndpoints.length > 0 ? (
            <div>
              {stats.topRiskEndpoints.map((ep, i) => (
                <div key={i} className="list-row list-row--compact">
                  <span className="mono">
                    <span style={{ color: "#22c55e" }}>{ep.method}</span>{" "}
                    {ep.path}
                  </span>
                  <span className="score" style={{ color: scoreColor(ep.score) }}>
                    {ep.score.toFixed(1)}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <p className="muted">Run a scan to compute risk scores.</p>
          )}
        </article>
      </div>

      <div className="responsive-grid">
        <article className="card">
          <h3 className="card-title">Targets</h3>
          {targets.length === 0 ? (
            <p className="muted">
              No targets yet. <Link to="/scan" className="subtle-link">Start a scan</Link>
            </p>
          ) : (
            <ul className="list">
              {targets.map((t) => (
                <li key={t.id} className="list-row">
                  <span>{t.domain}</span>
                  <Link to={`/graph?target=${t.id}`} className="subtle-link">
                    View Graph
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </article>

        <article className="card">
          <h3 className="card-title">Recent Scans</h3>
          {scans.length === 0 ? (
            <p className="muted">No scans yet.</p>
          ) : (
            <ul className="list">
              {scans.slice(0, 10).map((s) => (
                <li key={s.id} className="list-row list-row--compact">
                  <span>Scan #{s.id} ({s.scanType})</span>
                  <span className="score" style={{ color: statusColor(s.status) }}>
                    {s.status}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </article>
      </div>
    </section>
  );
}

function StatCard({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <div className="metric-card" style={{ borderLeftColor: color }}>
      <div className="metric-card__label">{label}</div>
      <div className="metric-card__value">{value}</div>
    </div>
  );
}

function scoreColor(score: number) {
  if (score >= 7) return "#ef4444";
  if (score >= 4) return "#f59e0b";
  return "#22c55e";
}

function statusColor(status: ScanJob["status"]) {
  if (status === "COMPLETED") return "#22c55e";
  if (status === "FAILED") return "#ef4444";
  return "#f59e0b";
}
