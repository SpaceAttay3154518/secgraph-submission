import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin

HEADER_SIGNATURES = {
    "Server": {
        "nginx": ("nginx", "server"),
        "Apache": ("Apache", "server"),
        "Microsoft-IIS": ("IIS", "server"),
        "gunicorn": ("Gunicorn", "server"),
        "Kestrel": ("Kestrel", "server"),
    },
    "X-Powered-By": {
        "Express": ("Express.js", "framework"),
        "PHP": ("PHP", "language"),
        "ASP.NET": ("ASP.NET", "framework"),
        "Django": ("Django", "framework"),
        "Flask": ("Flask", "framework"),
        "Next.js": ("Next.js", "framework"),
    }
}

HTML_SIGNATURES = [
    ("wp-content", "WordPress", "cms"),
    ("wp-includes", "WordPress", "cms"),
    ("/wp-admin", "WordPress", "cms"),
    ("__NEXT_DATA__", "Next.js", "framework"),
    ("__NUXT__", "Nuxt.js", "framework"),
    ("ng-app", "Angular", "framework"),
    ("ng-version", "Angular", "framework"),
    ("data-reactroot", "React", "framework"),
    ("_react", "React", "framework"),
    ("vue.js", "Vue.js", "framework"),
    ("data-v-", "Vue.js", "framework"),
    ("ember", "Ember.js", "framework"),
    ("jquery", "jQuery", "library"),
    ("bootstrap", "Bootstrap", "library"),
    ("tailwindcss", "Tailwind CSS", "library"),
    ("laravel", "Laravel", "framework"),
    ("csrf-token", "Rails", "framework"),
    ("drupal", "Drupal", "cms"),
    ("joomla", "Joomla", "cms"),
    ("shopify", "Shopify", "cms"),
    ("wix.com", "Wix", "cms"),
    ("squarespace", "Squarespace", "cms"),
]

COMMON_PATHS = [
    ("/robots.txt", None, None),
    ("/sitemap.xml", None, None),
    ("/wp-login.php", "WordPress", "cms"),
    ("/wp-admin/", "WordPress", "cms"),
    ("/phpmyadmin/", "phpMyAdmin", "tool"),
    ("/administrator/", "Joomla", "cms"),
    ("/user/login", "Drupal", "cms"),
    ("/.env", None, None),
    ("/graphql", "GraphQL", "api"),
    ("/api/swagger.json", "Swagger", "api"),
    ("/swagger-ui.html", "Swagger", "api"),
]

def fingerprint(url, timeout=15):
    technologies = {}

    try:
        resp = requests.get(url, timeout=timeout, allow_redirects=True,
                            headers={"User-Agent": "SecGraph/1.0 Security Scanner"})
    except requests.RequestException:
        return []

    for header_name, signatures in HEADER_SIGNATURES.items():
        header_value = resp.headers.get(header_name, "")
        for sig_key, (tech_name, category) in signatures.items():
            if sig_key.lower() in header_value.lower():
                version = _extract_version(header_value, sig_key)
                _add_tech(technologies, tech_name, version, category, 0.9)

    if "text/html" in resp.headers.get("Content-Type", ""):
        html_lower = resp.text.lower()
        soup = BeautifulSoup(resp.text, "lxml")

        for pattern, tech_name, category in HTML_SIGNATURES:
            if pattern.lower() in html_lower:
                _add_tech(technologies, tech_name, None, category, 0.7)

        generator = soup.find("meta", attrs={"name": "generator"})
        if generator and generator.get("content"):
            content = generator["content"]
            version = _extract_version_generic(content)
            _add_tech(technologies, content.split()[0], version, "cms", 0.95)

    for path, tech_name, category in COMMON_PATHS:
        try:
            check_url = urljoin(url, path)
            check_resp = requests.head(check_url, timeout=5, allow_redirects=True,
                                        headers={"User-Agent": "SecGraph/1.0 Security Scanner"})
            if check_resp.status_code == 200 and tech_name:
                _add_tech(technologies, tech_name, None, category, 0.8)
        except requests.RequestException:
            continue

    return [
        {
            "name": name,
            "version": info["version"],
            "category": info["category"],
            "confidence": info["confidence"]
        }
        for name, info in technologies.items()
    ]

def _add_tech(techs, name, version, category, confidence):
    if name in techs:
        techs[name]["confidence"] = max(techs[name]["confidence"], confidence)
        if version and not techs[name]["version"]:
            techs[name]["version"] = version
    else:
        techs[name] = {
            "version": version,
            "category": category,
            "confidence": confidence
        }

def _extract_version(header_value, tech_key):
    import re
    match = re.search(rf'{re.escape(tech_key)}[/\s]*([\d.]+)', header_value, re.IGNORECASE)
    return match.group(1) if match else None

def _extract_version_generic(text):
    import re
    match = re.search(r'(\d+\.\d+(?:\.\d+)?)', text)
    return match.group(1) if match else None
