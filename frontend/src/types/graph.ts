export interface GraphNode {
  id: string;
  label: string;
  type: "target" | "endpoint" | "technology" | "cve" | "parameter" | "vulnerability";
  group: string;
  score: number | null;
  data?: Record<string, unknown>;
}

export interface GraphLink {
  source: string;
  target: string;
  type: string;
  label: string;
}

export interface GraphData {
  nodes: GraphNode[];
  links: GraphLink[];
}
