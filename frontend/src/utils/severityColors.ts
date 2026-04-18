export const NODE_COLORS: Record<string, string> = {
  target: "#6366f1",
  endpoint: "#3b82f6",
  technology: "#22c55e",
  cve: "#ef4444",
  parameter: "#a855f7",
  vulnerability: "#f97316",
};

export const SEVERITY_COLORS: Record<string, string> = {
  CRITICAL: "#dc2626",
  HIGH: "#ef4444",
  MEDIUM: "#f59e0b",
  LOW: "#22c55e",
};

export function getNodeColor(type: string): string {
  return NODE_COLORS[type] || "#94a3b8";
}

export function getSeverityColor(severity: string): string {
  return SEVERITY_COLORS[severity] || "#94a3b8";
}

export function getNodeSize(type: string, score: number | null): number {
  const baseSize: Record<string, number> = {
    target: 12,
    endpoint: 6,
    technology: 8,
    cve: 8,
    parameter: 4,
    vulnerability: 7,
  };
  const base = baseSize[type] || 5;
  if (score && score > 0) {
    return base + score * 0.5;
  }
  return base;
}
