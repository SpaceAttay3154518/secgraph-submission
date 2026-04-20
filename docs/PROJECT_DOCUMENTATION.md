# SecGraph — Project Documentation

## 1. Project Summary

SecGraph is a security analysis tool for web targets. It scans a website, stores the collected data in a Neo4j graph database, and displays the results through a dashboard and an interactive knowledge graph.

The main idea behind the project is that security data becomes easier to understand when shown as connected nodes instead of flat lists. SecGraph links targets, endpoints, technologies, parameters, and CVEs together so the user can explore relationships and spot risks faster.

## 2. Objectives

- Discover endpoints, technologies, and parameters for a given target
- Store the results in a graph database to preserve relationships
- Provide a clear interface to explore the data
- Score endpoints by risk and highlight attack paths
- Allow data import from Burp Suite, Nmap, and OWASP ZAP

## 3. Architecture

The project is split into three services, all orchestrated with Docker Compose.

### Frontend

Built with React and TypeScript using Vite as the bundler. It serves as the main user interface with pages for the dashboard, scans, graph, endpoints, CVEs, and file imports. Also supports Android via Capacitor.

### Backend

Built with Spring Boot (Java 17). It exposes a REST API, manages graph operations in Neo4j, runs scoring algorithms, and handles data parsing from imported files.

### Recon Service

A lightweight Python Flask service responsible for crawling, header analysis, and technology fingerprinting. The backend calls this service during scans.

## 4. Features

### Dashboard

Gives a quick overview of a selected target: endpoint count, technology count, CVE count, parameter count, a severity distribution chart, and a list of the highest-risk endpoints.

### Scan Page

Lets the user enter a target URL, select crawl depth and scan type, then start a scan. Progress is tracked and displayed in real time.

### Knowledge Graph

Renders all discovered data as a force-directed graph. Nodes represent targets, endpoints, technologies, parameters, vulnerabilities, and CVEs. The user can also run attack path analysis to highlight risky chains on the graph.

### Endpoint Explorer

Lists all discovered endpoints with their risk score, HTTP method, parameters, vulnerabilities, and related CVEs.

### CVE View

Displays all collected CVEs with search by ID or description.

### Import Page

Accepts files from Burp Suite (XML), Nmap (XML), and OWASP ZAP (JSON). Imported data is parsed and stored in the graph like scan results.

## 5. API Routes

| Method | Route | Description |
|--------|-------|-------------|
| GET | `/api/targets` | List all scanned targets |
| GET | `/api/scans` | List all scan jobs |
| POST | `/api/scans` | Start a new scan |
| GET | `/api/graph/{targetId}` | Get graph data for a target |
| GET | `/api/stats/{targetId}` | Get dashboard statistics |
| GET | `/api/cves` | List all CVEs |
| GET | `/api/analysis/attack-paths/{targetId}` | Compute attack paths |
| GET | `/api/analysis/scores/{targetId}` | Compute endpoint risk scores |
| POST | `/api/import/burp` | Import a Burp Suite XML file |
| POST | `/api/import/nmap` | Import an Nmap XML file |
| POST | `/api/import/zap` | Import a ZAP JSON file |

## 6. Running the Project

From the project root:

```bash
docker compose up -d --build
```

After startup:

| Service | URL |
|---------|-----|
| Web app | `http://localhost:3000` |
| Backend API | `http://localhost:8080/api` |
| Neo4j Browser | `http://localhost:7474` |
| Recon health | `http://localhost:5000/recon/health` |

Neo4j credentials: `neo4j` / `secgraph123`

The backend starts with the `demo` profile by default, which loads sample data so the app can be tested without running a real scan first.

## 7. Android / Mobile

For the Android emulator, use `10.0.2.2` to reach the host machine. For a physical phone on the same network, use the computer's local IP address:

```bash
VITE_API_URL=http://192.168.1.100:8080/api npm run android:open:phone
```

Do not commit a real IP address — replace it only on your own machine during testing.

## 8. Project Structure

```
secgraph-submission/
├── backend/           Spring Boot API, graph logic, parsers
├── frontend/          React web app + Capacitor Android wrapper
├── recon-service/     Python Flask crawling and fingerprinting
├── docs/              Documentation and screenshots
└── docker-compose.yml
```

## 9. Known Limitations

- Scan quality depends on the recon service output and target accessibility
- Mobile testing requires correct local network setup
- Demo mode is useful for presentation but does not reflect real scan results

---

## Checking Todo List

Use this list to make sure everything works before submitting. Go through it step by step — should not take long.

- [ ] Run `docker compose up -d --build` and wait for all containers to start
- [ ] Open `http://localhost:3000` and check the dashboard loads properly
- [ ] Switch between targets in the dropdown and confirm the stats update
- [ ] Go to the Scan page, enter a URL, pick a scan type, and start a scan
- [ ] Wait for the scan to finish and check that the status shows COMPLETED
- [ ] Open the Graph page, select a target, and check that nodes appear on the canvas
- [ ] Click "Find Attack Paths" and verify the highlighted paths show up on the left panel
- [ ] Open the Endpoints page and make sure the risk scores and details are displayed
- [ ] Open the CVE page and try searching for a CVE by its ID
- [ ] Go to the Import page, upload a sample Burp/Nmap/ZAP file, and confirm no errors
- [ ] Check the API directly: `curl http://localhost:8080/api/targets` should return JSON
- [ ] Open Neo4j Browser at `http://localhost:7474`, log in, and run a quick Cypher query to see the graph data
- [ ] If testing on Android, make sure the phone and the computer are on the same network and the API URL is set correctly
- [ ] Check that no real IP addresses or credentials are committed in the repository
- [ ] Look at the console in the browser dev tools for any unexpected errors
