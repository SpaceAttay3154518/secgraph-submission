import { BrowserRouter, Routes, Route } from "react-router-dom";
import Layout from "./components/common/Layout";
import DashboardPage from "./pages/DashboardPage";
import ScanPage from "./pages/ScanPage";
import GraphPage from "./pages/GraphPage";
import CvePage from "./pages/CvePage";
import EndpointsPage from "./pages/EndpointsPage";
import ImportPage from "./pages/ImportPage";

export default function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/scan" element={<ScanPage />} />
          <Route path="/graph" element={<GraphPage />} />
          <Route path="/endpoints" element={<EndpointsPage />} />
          <Route path="/cves" element={<CvePage />} />
          <Route path="/import" element={<ImportPage />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}
