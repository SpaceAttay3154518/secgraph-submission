import { useState } from "react";
import ScanLauncher from "../components/scan/ScanLauncher";
import ScanProgress from "../components/scan/ScanProgress";
import { useNavigate } from "react-router-dom";

export default function ScanPage() {
  const [activeScanId, setActiveScanId] = useState<number | null>(null);
  const navigate = useNavigate();

  return (
    <section className="page page--narrow">
      <div className="page-header">
        <h1 className="page-title">Scan</h1>
      </div>

      <ScanLauncher onScanStarted={(id) => setActiveScanId(id)} />

      {activeScanId != null && (
        <div className="mb-24" style={{ marginTop: 16 }}>
          <ScanProgress
            scanId={activeScanId}
            onComplete={() => navigate("/graph")}
          />
        </div>
      )}
    </section>
  );
}
