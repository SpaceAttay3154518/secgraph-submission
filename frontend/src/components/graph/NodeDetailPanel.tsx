import type { GraphNode } from "../../types/graph";
import { getNodeColor, getSeverityColor } from "../../utils/severityColors";

interface Props {
  node: GraphNode | null;
  onClose: () => void;
}

export default function NodeDetailPanel({ node, onClose }: Props) {
  if (!node) return null;

  return (
    <aside className="node-panel" aria-label="Node details">
      <div className="node-panel__header">
        <div className="node-panel__type">
          <div className="node-panel__dot" style={{ background: getNodeColor(node.type) }} />
          <span style={{ fontSize: 12, textTransform: "uppercase", color: "#94a3b8" }}>
            {node.type}
          </span>
        </div>
        <button onClick={onClose} className="icon-button" aria-label="Close node details">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <path d="M6 6l12 12" />
            <path d="M18 6 6 18" />
          </svg>
        </button>
      </div>

      <h3 className="node-panel__label">{node.label}</h3>

      {node.score != null && (
        <div style={{ marginBottom: 8 }}>
          <span style={{ color: "#94a3b8", fontSize: 13 }}>Score: </span>
          <span style={{
            color: node.score >= 7 ? getSeverityColor("HIGH") :
                   node.score >= 4 ? getSeverityColor("MEDIUM") :
                   getSeverityColor("LOW"),
            fontWeight: "bold"
          }}>
            {node.score.toFixed(1)}
          </span>
        </div>
      )}

      <div style={{ fontSize: 13, color: "#94a3b8" }}>
        ID: {node.id}
      </div>
    </aside>
  );
}
