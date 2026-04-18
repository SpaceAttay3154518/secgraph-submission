import { useEffect, useState } from "react";
import { getTargets, getEndpointScores } from "../api/client";
import type { EndpointScore } from "../api/client";
import type { Target } from "../types/scan";
import EndpointExplorer from "../components/explorer/EndpointExplorer";

export default function EndpointsPage() {
  const [targets, setTargets] = useState<Target[]>([]);
  const [selectedTargetId, setSelectedTargetId] = useState<number | null>(null);
  const [endpoints, setEndpoints] = useState<EndpointScore[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    getTargets().then((data) => {
      setTargets(data);
      if (data.length > 0) setSelectedTargetId(data[0].id);
    });
  }, []);

  useEffect(() => {
    if (selectedTargetId == null) return;
    let cancelled = false;
    Promise.resolve().then(() => {
      if (!cancelled) setLoading(true);
    });
    getEndpointScores(selectedTargetId)
      .then((data) => {
        if (!cancelled) setEndpoints(data);
      })
      .catch(() => {
        if (!cancelled) setEndpoints([]);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [selectedTargetId]);

  return (
    <section className="page">
      <div className="page-header">
        <h1 className="page-title">Endpoints</h1>
        <select
          value={selectedTargetId ?? ""}
          onChange={(e) => setSelectedTargetId(Number(e.target.value))}
          className="select select--compact"
        >
          {targets.map((t) => (
            <option key={t.id} value={t.id}>{t.domain}</option>
          ))}
        </select>
      </div>

      <article className="card endpoint-panel">
        {loading ? (
          <p className="muted">Loading endpoints...</p>
        ) : (
          <EndpointExplorer endpoints={endpoints} />
        )}
      </article>
    </section>
  );
}
