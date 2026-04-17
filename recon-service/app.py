from flask import Flask, jsonify
from routes.scan_routes import scan_bp

app = Flask(__name__)
app.register_blueprint(scan_bp, url_prefix="/recon")

@app.route("/recon/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
