import { useEffect, useState, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import GraphVisualization from "../components/graph/GraphVisualization";
import NodeDetailPanel from "../components/graph/NodeDetailPanel";
import AttackPathHighlighter from "../components/graph/AttackPathHighlighter";
import { getGraph, getTargets, getAttackPaths } from "../api/client";
import type { GraphData, GraphNode } from "../types/graph";
import type { AttackPath } from "../api/client";
import type { Target } from "../types/scan";

export default function GraphPage() {
  const [searchParams] = useSearchParams();
  const [targets, setTargets] = useState<Target[]>([]);
  const [selectedTargetId, setSelectedTargetId] = useState<number | null>(null);
  const [graphData, setGraphData] = useState<GraphData | null>(null);
  const [selectedNode, setSelectedNode] = useState<GraphNode | null>(null);
  const [loading, setLoading] = useState(false);
  const [attackPaths, setAttackPaths] = useState<AttackPath[]>([]);
  const [selectedPathIndex, setSelectedPathIndex] = useState<number | null>(null);
  const [showPaths, setShowPaths] = useState(false);
  const [graphHeight, setGraphHeight] = useState(600);

  useEffect(() => {
    getTargets().then((data) => {
      setTargets(data);
      const paramId = searchParams.get("target");
      if (paramId) {
        setSelectedTargetId(Number(paramId));
      } else if (data.length > 0) {
        setSelectedTargetId(data[0].id);
      }
    });
  }, [searchParams]);

  useEffect(() => {
    if (selectedTargetId == null) return;
    let cancelled = false;
    Promise.resolve().then(() => {
      if (cancelled) return;
      setLoading(true);
      setAttackPaths([]);
      setSelectedPathIndex(null);
      setShowPaths(false);
    });
    getGraph(selectedTargetId)
      .then((data) => {
        if (!cancelled) setGraphData(data);
      })
      .catch(() => {
        if (!cancelled) setGraphData(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [selectedTargetId]);

  useEffect(() => {
    const updateHeight = () => {
      if (window.matchMedia("(max-width: 900px)").matches) {
        setGraphHeight(Math.max(420, Math.min(window.innerHeight - 210, 620)));
      } else {
        setGraphHeight(600);
      }
    };

    updateHeight();
    window.addEventListener("resize", updateHeight);
    return () => window.removeEventListener("resize", updateHeight);
  }, []);

  const handleFindPaths = useCallback(async () => {
    if (selectedTargetId == null) return;
    try {
      const paths = await getAttackPaths(selectedTargetId, 3.0, 10);
      setAttackPaths(paths);
      setShowPaths(true);
    } catch {
      setAttackPaths([]);
    }
  }, [selectedTargetId]);

  const highlightNodeIds = selectedPathIndex != null && attackPaths[selectedPathIndex]
    ? attackPaths[selectedPathIndex].nodes.map((n) => n.id)
    : [];

  return (
    <section className="page">
      <div className="page-header">
        <h1 className="page-title">Knowledge Graph</h1>

        <div className="page-actions">
          <button
            onClick={handleFindPaths}
            disabled={!selectedTargetId}
            className="button button--danger"
          >
            Find Attack Paths
          </button>

          <select
            value={selectedTargetId ?? ""}
            onChange={(e) => setSelectedTargetId(Number(e.target.value))}
            className="select select--compact"
          >
            <option value="">Select target...</option>
            {targets.map((t) => (
              <option key={t.id} value={t.id}>{t.domain}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="graph-stage">
        {loading && (
          <div className="graph-loading">
            Loading graph...
          </div>
        )}

        {graphData && graphData.nodes.length > 0 ? (
          <GraphVisualization
            data={graphData}
            onNodeClick={setSelectedNode}
            highlightPath={highlightNodeIds}
            height={graphHeight}
          />
        ) : !loading && (
          <div className="graph-empty" style={{ height: graphHeight }}>
            {selectedTargetId ? "No graph data available. Run a scan first." : "Select a target to view its graph."}
          </div>
        )}

        <NodeDetailPanel node={selectedNode} onClose={() => setSelectedNode(null)} />

        {showPaths && (
          <AttackPathHighlighter
            paths={attackPaths}
            selectedIndex={selectedPathIndex}
            onSelect={setSelectedPathIndex}
          />
        )}

        <div className={`graph-legend${selectedNode ? " graph-legend--with-panel" : ""}`}>
          {[
            { type: "target", color: "#6366f1" },
            { type: "endpoint", color: "#3b82f6" },
            { type: "technology", color: "#22c55e" },
            { type: "cve", color: "#ef4444" },
            { type: "parameter", color: "#a855f7" },
            { type: "vulnerability", color: "#f97316" },
          ].map(({ type, color }) => (
            <div key={type} className="graph-legend__item">
              <div className="graph-legend__dot" style={{ background: color }} />
              <span>{type}</span>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
