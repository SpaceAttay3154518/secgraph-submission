import type { AttackPath } from "../../api/client";

interface Props {
  paths: AttackPath[];
  selectedIndex: number | null;
  onSelect: (index: number | null) => void;
}

export default function AttackPathHighlighter({ paths, selectedIndex, onSelect }: Props) {
  if (paths.length === 0) return null;

  return (
    <aside className="attack-paths" aria-label="Attack paths">
      <h4 className="attack-paths__title">
        Attack Paths ({paths.length})
      </h4>

      {paths.map((path, i) => (
        <button
          type="button"
          key={i}
          onClick={() => onSelect(selectedIndex === i ? null : i)}
          className={`attack-path${selectedIndex === i ? " attack-path--active" : ""}`}
        >
          <div className="attack-path__summary">
            <span className="attack-path__entry mono">{path.entryPoint}</span>
            <span className="score" style={{ color: pathRiskColor(path.cvssScore) }}>
              {path.totalRisk.toFixed(1)}
            </span>
          </div>
          <div className="attack-path__cve">
            &rarr; {path.cveId} (CVSS {path.cvssScore})
          </div>
          {selectedIndex === i && (
            <div className="attack-path__nodes">
              {path.nodes.map((node, j) => (
                <span key={j} style={{ color: "#94a3b8" }}>
                  {j > 0 && <span style={{ color: "#ef4444" }}> &rarr; </span>}
                  <span style={{ color: nodeColor(node.type) }}>{node.label}</span>
                </span>
              ))}
            </div>
          )}
        </button>
      ))}
    </aside>
  );
}

function pathRiskColor(cvssScore: number) {
  if (cvssScore >= 9) return "#ef4444";
  if (cvssScore >= 7) return "#f59e0b";
  return "#22c55e";
}

function nodeColor(type: string): string {
  const colors: Record<string, string> = {
    target: "#6366f1",
    endpoint: "#3b82f6",
    technology: "#22c55e",
    cve: "#ef4444",
    parameter: "#a855f7",
    vulnerabilitytype: "#f97316",
  };
  return colors[type] || "#94a3b8";
}
