import { useState, useCallback } from "react";
import { getRequestErrorMessage, importFile } from "../api/client";

type ImportType = "burp" | "nmap" | "zap";

export default function ImportPage() {
  const [domain, setDomain] = useState("");
  const [fileType, setFileType] = useState<ImportType>("burp");
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<Record<string, unknown> | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [dragOver, setDragOver] = useState(false);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    const droppedFile = e.dataTransfer.files[0];
    if (droppedFile) {
      setFile(droppedFile);
      const name = droppedFile.name.toLowerCase();
      if (name.includes("burp") || name.endsWith(".xml")) setFileType("burp");
      else if (name.includes("nmap")) setFileType("nmap");
      else if (name.endsWith(".json")) setFileType("zap");
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file || !domain) return;

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const res = await importFile(fileType, domain, file);
      setResult(res);
    } catch (err: unknown) {
      setError(getRequestErrorMessage(err, "Import failed"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="page page--narrow">
      <div className="page-header">
        <h1 className="page-title">Import Tool Data</h1>
      </div>

      <form onSubmit={handleSubmit} className="form-card">
        <div className="field">
          <label className="field-label">
            Target Domain
          </label>
          <input
            type="text"
            value={domain}
            onChange={(e) => setDomain(e.target.value)}
            placeholder="example.com"
            required
            className="input"
          />
        </div>

        <div className="field">
          <label className="field-label">
            Import Source
          </label>
          <select
            value={fileType}
            onChange={(e) => setFileType(e.target.value as ImportType)}
            className="select"
          >
            <option value="burp">Burp Suite (XML)</option>
            <option value="nmap">Nmap (XML)</option>
            <option value="zap">OWASP ZAP (JSON)</option>
          </select>
        </div>

        <div
          onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={() => setDragOver(false)}
          onDrop={handleDrop}
          onClick={() => document.getElementById("file-input")?.click()}
          className={`drop-zone${dragOver ? " drop-zone--active" : ""}`}
        >
          <input
            id="file-input"
            type="file"
            accept=".xml,.json"
            onChange={(e) => setFile(e.target.files?.[0] || null)}
            style={{ display: "none" }}
          />
          {file ? (
            <div>
              <div style={{ fontWeight: 600 }}>{file.name}</div>
              <div style={{ color: "#94a3b8", fontSize: 13 }}>
                {(file.size / 1024).toFixed(1)} KB
              </div>
            </div>
          ) : (
            <div className="muted">
              Drop a file here or click to browse
            </div>
          )}
        </div>

        {error && <div className="error-text">{error}</div>}

        <button
          type="submit"
          disabled={loading || !file || !domain}
          className={`button ${loading ? "button--muted" : "button--primary"}`}
        >
          {loading ? "Importing..." : "Import"}
        </button>
      </form>

      {result && (
        <div className="card import-result">
          <h3 className="import-result__title">Import Successful</h3>
          <div>
            {Object.entries(result).map(([key, value]) => (
              <div key={key} className="key-value-row">
                <span style={{ color: "#94a3b8" }}>{key}: </span>
                <span>{String(value)}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </section>
  );
}
