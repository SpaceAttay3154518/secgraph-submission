import type { EndpointScore } from "../../api/client";

interface Props {
  endpoints: EndpointScore[];
}

export default function EndpointExplorer({ endpoints }: Props) {
  if (endpoints.length === 0) {
    return <p className="muted">No endpoint data available.</p>;
  }

  return (
    <>
      <div className="endpoint-table-wrap">
      <table className="endpoint-table">
        <thead>
          <tr>
            <th>Method</th>
            <th>Path</th>
            <th>Risk Score</th>
            <th>Params</th>
            <th>Vulns</th>
            <th>CVEs</th>
            <th>Data Flow</th>
          </tr>
        </thead>
        <tbody>
          {endpoints.map((ep) => (
            <tr key={ep.endpointId}>
              <td>
                <span className="pill" style={{ background: methodColor(ep.method) }}>
                  {ep.method}
                </span>
              </td>
              <td className="mono">{ep.path}</td>
              <td>
                <span className="score" style={{ color: scoreColor(ep.score) }}>
                  {ep.score.toFixed(1)}
                </span>
              </td>
              <td>{ep.paramCount}</td>
              <td>{ep.vulnCount}</td>
              <td>{ep.relatedCves}</td>
              <td>{ep.flowDegree}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>

      <div className="endpoint-cards">
        {endpoints.map((ep) => (
          <article key={ep.endpointId} className="endpoint-card">
            <div className="endpoint-card__head">
              <span className="pill" style={{ background: methodColor(ep.method) }}>
                {ep.method}
              </span>
              <span className="endpoint-card__path mono">{ep.path}</span>
            </div>
            <div className="endpoint-card__grid">
              <Metric label="Risk" value={ep.score.toFixed(1)} color={scoreColor(ep.score)} />
              <Metric label="Params" value={String(ep.paramCount)} />
              <Metric label="Vulns" value={String(ep.vulnCount)} />
              <Metric label="CVEs" value={String(ep.relatedCves)} />
            </div>
          </article>
        ))}
      </div>
    </>
  );
}

function Metric({ label, value, color }: { label: string; value: string; color?: string }) {
  return (
    <div className="endpoint-card__metric">
      <span>{label}</span>
      <strong style={{ color }}>{value}</strong>
    </div>
  );
}

function methodColor(method: string): string {
  const colors: Record<string, string> = {
    GET: "#22c55e",
    POST: "#3b82f6",
    PUT: "#f59e0b",
    DELETE: "#ef4444",
    PATCH: "#a855f7",
  };
  return colors[method] || "#64748b";
}

function scoreColor(score: number) {
  if (score >= 7) return "#ef4444";
  if (score >= 4) return "#f59e0b";
  return "#22c55e";
}
