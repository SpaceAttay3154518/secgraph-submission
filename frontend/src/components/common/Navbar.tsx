import { Link, useLocation } from "react-router-dom";
import type { ReactNode } from "react";

const navItems = [
  { to: "/", label: "Dashboard", icon: <DashboardIcon /> },
  { to: "/scan", label: "Scan", icon: <ScanIcon /> },
  { to: "/graph", label: "Graph", icon: <GraphIcon /> },
  { to: "/endpoints", label: "Endpoints", icon: <EndpointsIcon /> },
  { to: "/cves", label: "CVEs", icon: <CveIcon /> },
  { to: "/import", label: "Import", icon: <ImportIcon /> },
];

export default function Navbar() {
  const location = useLocation();

  return (
    <nav className="navbar" aria-label="Primary navigation">
      <div className="navbar__inner">
        <Link to="/" className="navbar__brand">
          <span className="navbar__brand-mark">Sec</span>Graph
        </Link>

        <div className="navbar__links">
          {navItems.map((item) => {
            const isActive = location.pathname === item.to;
            return (
              <Link
                key={item.to}
                to={item.to}
                aria-current={isActive ? "page" : undefined}
                className={`navbar__link${isActive ? " navbar__link--active" : ""}`}
              >
                <span className="navbar__icon" aria-hidden="true">
                  {item.icon}
                </span>
                {item.label}
              </Link>
            );
          })}
        </div>
      </div>
    </nav>
  );
}

function Icon({ children }: { children: ReactNode }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      {children}
    </svg>
  );
}

function DashboardIcon() {
  return (
    <Icon>
      <path d="M4 13h6V4H4v9Z" />
      <path d="M14 20h6v-9h-6v9Z" />
      <path d="M4 20h6v-3H4v3Z" />
      <path d="M14 7h6V4h-6v3Z" />
    </Icon>
  );
}

function ScanIcon() {
  return (
    <Icon>
      <path d="M5 5h5" />
      <path d="M14 5h5" />
      <path d="M5 19h5" />
      <path d="M14 19h5" />
      <path d="M5 5v5" />
      <path d="M19 5v5" />
      <path d="M5 14v5" />
      <path d="M19 14v5" />
      <path d="M9 12h6" />
    </Icon>
  );
}

function GraphIcon() {
  return (
    <Icon>
      <circle cx="6" cy="7" r="2.5" />
      <circle cx="18" cy="7" r="2.5" />
      <circle cx="12" cy="18" r="2.5" />
      <path d="m8.2 8.7 2.6 6.1" />
      <path d="m15.8 8.7-2.6 6.1" />
      <path d="M8.5 7h7" />
    </Icon>
  );
}

function EndpointsIcon() {
  return (
    <Icon>
      <path d="M4 6h16" />
      <path d="M4 12h16" />
      <path d="M4 18h16" />
      <path d="M8 6v12" />
    </Icon>
  );
}

function CveIcon() {
  return (
    <Icon>
      <path d="M12 3 4 6v6c0 4.2 3.4 7.2 8 9 4.6-1.8 8-4.8 8-9V6l-8-3Z" />
      <path d="M12 8v5" />
      <path d="M12 16h.01" />
    </Icon>
  );
}

function ImportIcon() {
  return (
    <Icon>
      <path d="M12 3v12" />
      <path d="m8 11 4 4 4-4" />
      <path d="M5 19h14" />
    </Icon>
  );
}
