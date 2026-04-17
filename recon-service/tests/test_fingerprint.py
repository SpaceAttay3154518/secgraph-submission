import unittest
from unittest.mock import patch, MagicMock
from scanners.tech_fingerprint import fingerprint

class TestFingerprint(unittest.TestCase):

    @patch("scanners.tech_fingerprint.requests.head")
    @patch("scanners.tech_fingerprint.requests.get")
    def test_detects_server_header(self, mock_get, mock_head):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.headers = {
            "Content-Type": "text/html",
            "Server": "nginx/1.21.3",
            "X-Powered-By": "Express"
        }
        mock_response.text = "<html><body>Hello</body></html>"
        mock_get.return_value = mock_response

        mock_head_response = MagicMock()
        mock_head_response.status_code = 404
        mock_head.return_value = mock_head_response

        result = fingerprint("https://example.com", timeout=5)
        names = [t["name"] for t in result]

        self.assertIn("nginx", names)
        self.assertIn("Express.js", names)

        nginx_tech = next(t for t in result if t["name"] == "nginx")
        self.assertEqual(nginx_tech["version"], "1.21.3")

    @patch("scanners.tech_fingerprint.requests.head")
    @patch("scanners.tech_fingerprint.requests.get")
    def test_detects_html_patterns(self, mock_get, mock_head):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.headers = {"Content-Type": "text/html"}
        mock_response.text = """
        <html>
        <head><script src="/wp-includes/js/jquery.js"></script></head>
        <body>
            <link rel="stylesheet" href="/wp-content/themes/style.css"/>
        </body>
        </html>
        """
        mock_get.return_value = mock_response

        mock_head_response = MagicMock()
        mock_head_response.status_code = 404
        mock_head.return_value = mock_head_response

        result = fingerprint("https://example.com", timeout=5)
        names = [t["name"] for t in result]

        self.assertIn("WordPress", names)
        self.assertIn("jQuery", names)

class TestHeaderAnalyzer(unittest.TestCase):

    @patch("scanners.header_analyzer.requests.get")
    def test_scores_security_headers(self, mock_get):
        from scanners.header_analyzer import analyze_headers

        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.headers = {
            "Content-Security-Policy": "default-src 'self'",
            "Strict-Transport-Security": "max-age=31536000; includeSubDomains; preload",
            "X-Frame-Options": "DENY",
            "X-Content-Type-Options": "nosniff",
        }
        mock_get.return_value = mock_response

        result = analyze_headers("https://example.com", timeout=5)

        self.assertGreater(result["score"], 50)
        self.assertTrue(len(result["issues"]) > 0)  # Still missing some headers

if __name__ == "__main__":
    unittest.main()
