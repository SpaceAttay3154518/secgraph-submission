import requests

SECURITY_HEADERS = {
    "Content-Security-Policy": {
        "weight": 25,
        "description": "Prevents XSS, clickjacking, and code injection attacks"
    },
    "Strict-Transport-Security": {
        "weight": 20,
        "description": "Enforces HTTPS connections"
    },
    "X-Frame-Options": {
        "weight": 15,
        "description": "Prevents clickjacking attacks"
    },
    "X-Content-Type-Options": {
        "weight": 10,
        "description": "Prevents MIME type sniffing"
    },
    "Referrer-Policy": {
        "weight": 10,
        "description": "Controls referrer information leakage"
    },
    "Permissions-Policy": {
        "weight": 10,
        "description": "Controls browser features and APIs"
    },
    "X-XSS-Protection": {
        "weight": 5,
        "description": "Legacy XSS filter (deprecated but still checked)"
    },
    "Cross-Origin-Opener-Policy": {
        "weight": 5,
        "description": "Prevents cross-origin attacks on window references"
    },
}

def analyze_headers(url, timeout=15):
    try:
        resp = requests.get(url, timeout=timeout, allow_redirects=True,
                            headers={"User-Agent": "SecGraph/1.0 Security Scanner"})
    except requests.RequestException as e:
        return {"headers": {}, "score": 0, "issues": [f"Request failed: {str(e)}"]}

    headers_found = {}
    issues = []
    score = 0

    for header_name, info in SECURITY_HEADERS.items():
        value = resp.headers.get(header_name)
        headers_found[header_name] = value

        if value:
            score += info["weight"]
        else:
            issues.append(f"Missing {header_name}: {info['description']}")

    server = resp.headers.get("Server", "")
    if server and any(c.isdigit() for c in server):
        issues.append(f"Server header reveals version: {server}")

    powered_by = resp.headers.get("X-Powered-By", "")
    if powered_by:
        issues.append(f"X-Powered-By header reveals technology: {powered_by}")

    hsts = resp.headers.get("Strict-Transport-Security", "")
    if hsts:
        if "includeSubDomains" not in hsts:
            issues.append("HSTS missing includeSubDomains directive")
        if "preload" not in hsts:
            issues.append("HSTS missing preload directive")

    return {
        "headers": headers_found,
        "score": score,
        "issues": issues
    }
