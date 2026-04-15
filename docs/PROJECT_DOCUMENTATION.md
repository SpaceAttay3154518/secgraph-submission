# Project Documentation

## 1. Project Summary

SecGraph is a security analysis application for web targets. It combines scanning, graph storage, and visual analysis in one project. The user can work from a browser or from an Android phone.

The project is built around one main idea: security information is easier to understand when it is connected. Instead of showing only lists, SecGraph builds links between targets, endpoints, technologies, parameters, and CVEs.

## 2. Main Objectives

- Discover data about a target
- Save that data in a graph database
- Show the result in a clear interface
- Help the user identify high-risk endpoints
- Show possible attack paths
- Support imported data from common security tools

## 3. System Architecture

The project has three main parts:

### Frontend

The frontend is made with React and TypeScript. It provides the user interface for the dashboard, scan page, graph page, CVE page, endpoint page, and import page.

### Backend

The backend is built with Spring Boot. It exposes the REST API, manages graph data in Neo4j, calculates scores, and returns the data needed by the frontend.

### Recon Service

The recon service is a small Python Flask service. It performs scanning tasks such as crawl, header analysis, and technology fingerprinting.

## 4. Main Features

### Dashboard

Shows:

- Number of endpoints
- Number of technologies
- Number of CVEs
- Number of parameters
- CVE severity chart
- Top risk endpoints
- Recent scans

### Scan Page

Allows the user to:

- Enter a target URL
- Choose crawl depth
- Choose scan type
- Start a scan and follow the progress

### Knowledge Graph

Shows the relationship between:

- Target
- Endpoint
- Technology
- Parameter
- Vulnerability
- CVE

The graph page can also request attack path analysis for the selected target.

### Endpoint Explorer

Shows scored endpoints with:

- Path
- Method
- Risk score
- Number of parameters
- Number of vulnerabilities
- Related CVEs

### CVE View

Shows CVEs and allows basic search by ID or description.

### Import Page

Supports:

- Burp XML
- Nmap XML
- OWASP ZAP JSON

## 5. Important API Routes

Main routes used by the frontend:

- `GET /api/targets`
- `GET /api/scans`
- `POST /api/scans`
- `GET /api/graph/{targetId}`
- `GET /api/stats/{targetId}`
- `GET /api/cves`
- `GET /api/analysis/attack-paths/{targetId}`
- `GET /api/analysis/scores/{targetId}`
- `POST /api/import/burp`
- `POST /api/import/nmap`
- `POST /api/import/zap`

## 6. Demo Data

When the backend runs with the `demo` profile, the application loads sample data at startup. This helps the user test the dashboard, graph, endpoints, and CVE views without waiting for a full real scan.

## 7. How To Run

From the root directory:

```bash
docker compose up -d --build
```

Services:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080/api`
- Neo4j Browser: `http://localhost:7474`
- Recon service: `http://localhost:5000/recon/health`

## 8. How To Test

### Manual UI Test

Use this simple checklist:

1. Open the dashboard and confirm that the page loads.
2. Confirm that the target selector works.
3. Start a new scan from the scan page.
4. Open the graph page and load one target.
5. Click `Find Attack Paths`.
6. Open the endpoints page and confirm score values are visible.
7. Open the CVE page and test the search field.
8. Upload a supported file on the import page.

### API Test

Examples:

```bash
curl http://localhost:8080/api/targets
curl http://localhost:8080/api/scans
curl http://localhost:8080/api/stats/41
curl http://localhost:8080/api/graph/41
curl http://localhost:8080/api/analysis/scores/41
```

## 9. Android And IP Usage

For mobile testing, the IP setup is important.

### Emulator

The Android emulator does not use the same `localhost` as the host machine. For this reason, the emulator should use:

```text
10.0.2.2
```

This address points from the emulator to the computer that runs the backend.

### Real Phone

For a real phone, `localhost` will not work. The phone must use the IP address of the computer on the local network.

Example:

```text
http://192.168.1.100:8080/api
```

Rules:

- The phone and the computer must be on the same network
- The backend must be running
- The repository should not contain a real personal IP address
- Use an example private IP in shared documentation and replace it only during local testing

## 10. Project Structure

### `backend/`

- Controllers
- Services
- Repositories
- Models
- Graph analysis classes

### `frontend/`

- Pages
- Reusable components
- API client
- Android Capacitor project

### `recon-service/`

- Flask app
- Scan routes
- Crawling and fingerprint logic

## 11. Known Limits

- Real scan quality depends on the recon service output
- Mobile testing needs correct local network access
- Demo mode is useful for presentation, but it is not the same as a full production deployment

## 12. Conclusion

SecGraph provides a simple but complete workflow for security exploration. It can scan a target, store the results in a graph, and show the result in clear views. The project also supports Android access, which makes the system easier to present and test on a phone.
