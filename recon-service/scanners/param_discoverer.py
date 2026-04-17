import requests
from bs4 import BeautifulSoup
from urllib.parse import urlparse, parse_qs
import json
import re

def discover_params(url, timeout=15):
    params = []

    try:
        resp = requests.get(url, timeout=timeout, allow_redirects=True,
                            headers={"User-Agent": "SecGraph/1.0 Security Scanner"})
    except requests.RequestException:
        return params

    parsed = urlparse(url)
    for name in parse_qs(parsed.query):
        params.append({"name": name, "type": "string", "location": "query"})

    content_type = resp.headers.get("Content-Type", "")

    if "text/html" in content_type:
        soup = BeautifulSoup(resp.text, "lxml")
        for form in soup.find_all("form"):
            method = form.get("method", "GET").upper()
            location = "body" if method == "POST" else "query"

            for inp in form.find_all(["input", "textarea", "select"]):
                name = inp.get("name")
                if name:
                    params.append({
                        "name": name,
                        "type": inp.get("type", "text"),
                        "location": location
                    })

        for elem in soup.find_all(attrs={"data-api": True}):
            api_url = elem.get("data-api")
            if api_url:
                for name in parse_qs(urlparse(api_url).query):
                    params.append({"name": name, "type": "string", "location": "query"})

    if "application/json" in content_type:
        try:
            data = resp.json()
            if isinstance(data, dict):
                for key in data:
                    params.append({"name": key, "type": _infer_type(data[key]), "location": "body"})
        except (json.JSONDecodeError, ValueError):
            pass

    seen = set()
    unique = []
    for p in params:
        key = (p["name"], p["location"])
        if key not in seen:
            seen.add(key)
            unique.append(p)

    return unique

def _infer_type(value):
    if isinstance(value, bool):
        return "boolean"
    if isinstance(value, int):
        return "integer"
    if isinstance(value, float):
        return "number"
    if isinstance(value, list):
        return "array"
    if isinstance(value, dict):
        return "object"
    return "string"
