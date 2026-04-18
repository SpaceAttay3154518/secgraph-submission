import { useState } from "react";
import { getRequestErrorMessage, startScan } from "../../api/client";

interface Props {
  onScanStarted: (scanId: number) => void;
}

export default function ScanLauncher({ onScanStarted }: Props) {
  const [url, setUrl] = useState("");
  const [depth, setDepth] = useState(2);
  const [scanType, setScanType] = useState<"FULL" | "CRAWL_ONLY" | "FINGERPRINT_ONLY">("FULL");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const result = await startScan({ url, depth, scanType });
      onScanStarted(result.scanId);
      setUrl("");
    } catch (err: unknown) {
      setError(getRequestErrorMessage(err, "Failed to start scan"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="form-card">
      <h2 className="form-title">New Scan</h2>

      <div className="field">
        <label className="field-label">
          Target URL
        </label>
        <input
          type="url"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          placeholder="https://example.com"
          required
          className="input"
        />
      </div>

      <div className="form-row">
        <div className="field">
          <label className="field-label">
            Depth
          </label>
          <select
            value={depth}
            onChange={(e) => setDepth(Number(e.target.value))}
            className="select"
          >
            <option value={1}>1</option>
            <option value={2}>2</option>
            <option value={3}>3</option>
          </select>
        </div>

        <div className="field">
          <label className="field-label">
            Scan Type
          </label>
          <select
            value={scanType}
            onChange={(e) => setScanType(e.target.value as typeof scanType)}
            className="select"
          >
            <option value="FULL">Full Scan</option>
            <option value="CRAWL_ONLY">Crawl Only</option>
            <option value="FINGERPRINT_ONLY">Fingerprint Only</option>
          </select>
        </div>
      </div>

      {error && (
        <div className="error-text">{error}</div>
      )}

      <button
        type="submit"
        disabled={loading}
        className={`button ${loading ? "button--muted" : "button--primary"}`}
      >
        {loading ? "Starting..." : "Start Scan"}
      </button>
    </form>
  );
}
