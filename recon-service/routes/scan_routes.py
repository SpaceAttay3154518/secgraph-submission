from flask import Blueprint, request, jsonify
from scanners.crawler import crawl
from scanners.tech_fingerprint import fingerprint
from scanners.header_analyzer import analyze_headers
from scanners.param_discoverer import discover_params

scan_bp = Blueprint("scan", __name__)

@scan_bp.route("/crawl", methods=["POST"])
def crawl_target():
    data = request.get_json()
    url = data.get("url")
    depth = data.get("depth", 2)
    timeout = data.get("timeout", 30)

    if not url:
        return jsonify({"error": "url is required"}), 400

    endpoints = crawl(url, max_depth=depth, timeout=timeout)
    return jsonify({"endpoints": endpoints})

@scan_bp.route("/fingerprint", methods=["POST"])
def fingerprint_target():
    data = request.get_json()
    url = data.get("url")

    if not url:
        return jsonify({"error": "url is required"}), 400

    technologies = fingerprint(url)
    return jsonify({"technologies": technologies})

@scan_bp.route("/headers", methods=["POST"])
def analyze_headers_route():
    data = request.get_json()
    url = data.get("url")

    if not url:
        return jsonify({"error": "url is required"}), 400

    result = analyze_headers(url)
    return jsonify(result)

@scan_bp.route("/params", methods=["POST"])
def discover_params_route():
    data = request.get_json()
    url = data.get("url")

    if not url:
        return jsonify({"error": "url is required"}), 400

    params = discover_params(url)
    return jsonify({"params": params})
