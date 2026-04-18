import axios from "axios";
import type { GraphData } from "../types/graph";
import type { ScanRequest, ScanJob, Target } from "../types/scan";
import type { Cve } from "../types/cve";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
});

export function getRequestErrorMessage(error: unknown, fallback: string) {
  if (axios.isAxiosError<{ message?: string }>(error)) {
    return error.response?.data?.message || fallback;
  }
  return fallback;
}

export async function startScan(request: ScanRequest) {
  const { data } = await api.post<{ scanId: number; status: string }>("/scans", request);
  return data;
}

export async function getScan(id: number) {
  const { data } = await api.get<ScanJob>(`/scans/${id}`);
  return data;
}

export async function getAllScans() {
  const { data } = await api.get<ScanJob[]>("/scans");
  return data;
}

export async function getTargets() {
  const { data } = await api.get<Target[]>("/targets");
  return data;
}

export async function getTarget(id: number) {
  const { data } = await api.get<Target>(`/targets/${id}`);
  return data;
}

export async function getGraph(targetId: number) {
  const { data } = await api.get<GraphData>(`/graph/${targetId}`);
  return data;
}

export async function getCves() {
  const { data } = await api.get<Cve[]>("/cves");
  return data;
}

export async function getCvesByTarget(domain: string) {
  const { data } = await api.get<Cve[]>(`/cves/target/${domain}`);
  return data;
}

export interface TargetStats {
  endpoints: number;
  technologies: number;
  cves: number;
  parameters: number;
  severityBreakdown: Record<string, number>;
  topRiskEndpoints: { path: string; method: string; score: number }[];
}

export async function getStats(targetId: number) {
  const { data } = await api.get<TargetStats>(`/stats/${targetId}`);
  return data;
}

export interface AttackPath {
  nodes: { id: string; type: string; label: string }[];
  totalRisk: number;
  entryPoint: string;
  cveId: string;
  cvssScore: number;
}

export interface EndpointScore {
  endpointId: number;
  path: string;
  method: string;
  score: number;
  paramCount: number;
  vulnCount: number;
  flowDegree: number;
  relatedCves: number;
}

export async function getAttackPaths(targetId: number, minCvss = 5.0, limit = 10) {
  const { data } = await api.get<AttackPath[]>(`/analysis/attack-paths/${targetId}`, {
    params: { minCvss, limit },
  });
  return data;
}

export async function getEndpointScores(targetId: number) {
  const { data } = await api.get<EndpointScore[]>(`/analysis/scores/${targetId}`);
  return data;
}

export async function importFile(type: "burp" | "nmap" | "zap", domain: string, file: File) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("domain", domain);
  const { data } = await api.post(`/import/${type}`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
}
