import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin, urlparse, parse_qs
from collections import deque

def crawl(base_url, max_depth=2, timeout=30):
    parsed_base = urlparse(base_url)
    base_domain = parsed_base.netloc
    base_scheme = parsed_base.scheme

    visited = set()
    endpoints = []
    queue = deque([(base_url, 0)])

    while queue and len(endpoints) < 200:
        url, depth = queue.popleft()

        if depth > max_depth:
            continue

        normalized = _normalize_url(url)
        if normalized in visited:
            continue
        visited.add(normalized)

        try:
            resp = requests.get(url, timeout=timeout, allow_redirects=True,
                                headers={"User-Agent": "SecGraph/1.0 Security Scanner"})
        except requests.RequestException:
            continue

        parsed = urlparse(url)
        path = parsed.path or "/"

        params = []
        for name, values in parse_qs(parsed.query).items():
            params.append({
                "name": name,
                "type": "string",
                "location": "query"
            })

        endpoint = {
            "path": path,
            "method": "GET",
            "statusCode": resp.status_code,
            "contentType": resp.headers.get("Content-Type", ""),
            "params": params
        }
        endpoints.append(endpoint)

        if "text/html" in resp.headers.get("Content-Type", ""):
            soup = BeautifulSoup(resp.text, "lxml")

            for tag in soup.find_all("a", href=True):
                link = urljoin(url, tag["href"])
                link_parsed = urlparse(link)
                if link_parsed.netloc == base_domain:
                    queue.append((link, depth + 1))

            for form in soup.find_all("form"):
                action = form.get("action", "")
                method = form.get("method", "GET").upper()
                form_url = urljoin(url, action) if action else url
                form_parsed = urlparse(form_url)

                if form_parsed.netloc != base_domain:
                    continue

                form_params = []
                for inp in form.find_all(["input", "textarea", "select"]):
                    name = inp.get("name")
                    if name:
                        form_params.append({
                            "name": name,
                            "type": inp.get("type", "text"),
                            "location": "body" if method == "POST" else "query"
                        })

                form_endpoint = {
                    "path": form_parsed.path or "/",
                    "method": method,
                    "statusCode": None,
                    "contentType": None,
                    "params": form_params
                }
                endpoints.append(form_endpoint)

            for script in soup.find_all("script", src=False):
                if script.string:
                    _extract_api_paths(script.string, endpoints, base_domain)

    return _deduplicate_endpoints(endpoints)

def _normalize_url(url):
    parsed = urlparse(url)
    return f"{parsed.scheme}://{parsed.netloc}{parsed.path}"

def _extract_api_paths(script_text, endpoints, base_domain):
    import re
    patterns = [
        r'["\'](/api/[a-zA-Z0-9/_-]+)["\']',
        r'["\'](/v\d+/[a-zA-Z0-9/_-]+)["\']',
        r'fetch\(["\']([^"\']+)["\']',
        r'axios\.\w+\(["\']([^"\']+)["\']',
    ]

    for pattern in patterns:
        for match in re.finditer(pattern, script_text):
            path = match.group(1)
            parsed = urlparse(path)
            if not parsed.netloc or parsed.netloc == base_domain:
                endpoints.append({
                    "path": parsed.path or path,
                    "method": "GET",
                    "statusCode": None,
                    "contentType": None,
                    "params": []
                })

def _deduplicate_endpoints(endpoints):
    seen = set()
    unique = []
    for ep in endpoints:
        key = (ep["path"], ep["method"])
        if key not in seen:
            seen.add(key)
            unique.append(ep)
    return unique
