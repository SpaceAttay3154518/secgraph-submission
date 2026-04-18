interface Technology {
  id: number;
  name: string;
  version: string | null;
  category: string;
  confidence: number | null;
}

interface Props {
  technologies: Technology[];
}

const CATEGORY_COLORS: Record<string, string> = {
  framework: "#6366f1",
  server: "#3b82f6",
  language: "#22c55e",
  cms: "#f59e0b",
  library: "#a855f7",
  tool: "#ec4899",
  service: "#14b8a6",
  os: "#64748b",
  api: "#f97316",
};

export default function TechStackView({ technologies }: Props) {
  if (technologies.length === 0) {
    return <p style={{ color: "#64748b" }}>No technologies detected.</p>;
  }

  const grouped = technologies.reduce((acc, tech) => {
    const cat = tech.category || "other";
    if (!acc[cat]) acc[cat] = [];
    acc[cat].push(tech);
    return acc;
  }, {} as Record<string, Technology[]>);

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
      {Object.entries(grouped).map(([category, techs]) => (
        <div key={category}>
          <h4 style={{
            margin: "0 0 8px",
            fontSize: 13,
            color: CATEGORY_COLORS[category] || "#94a3b8",
            textTransform: "uppercase",
            letterSpacing: 1,
          }}>
            {category}
          </h4>
          <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>
            {techs.map((tech) => (
              <div key={tech.id} style={{
                background: "#0f172a",
                border: `1px solid ${CATEGORY_COLORS[category] || "#334155"}`,
                borderRadius: 6,
                padding: "8px 12px",
                fontSize: 13,
              }}>
                <span style={{ fontWeight: 600 }}>{tech.name}</span>
                {tech.version && (
                  <span style={{ color: "#94a3b8", marginLeft: 4 }}>v{tech.version}</span>
                )}
                {tech.confidence != null && (
                  <span style={{
                    color: "#64748b",
                    fontSize: 11,
                    marginLeft: 8,
                  }}>
                    {Math.round(tech.confidence * 100)}%
                  </span>
                )}
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
