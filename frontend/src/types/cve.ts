export interface Cve {
  id: number;
  cveId: string;
  cvssScore: number;
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  description: string;
  publishedDate: string | null;
}
