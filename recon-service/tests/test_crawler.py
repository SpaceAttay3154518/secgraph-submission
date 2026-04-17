import unittest
from unittest.mock import patch, MagicMock
from scanners.crawler import crawl, _deduplicate_endpoints, _normalize_url

class TestCrawlerHelpers(unittest.TestCase):

    def test_normalize_url_strips_query(self):
        result = _normalize_url("https://example.com/page?foo=bar")
        self.assertEqual(result, "https://example.com/page")

    def test_deduplicate_endpoints(self):
        endpoints = [
            {"path": "/api/users", "method": "GET", "statusCode": 200, "contentType": "text/html", "params": []},
            {"path": "/api/users", "method": "GET", "statusCode": 200, "contentType": "text/html", "params": []},
            {"path": "/api/users", "method": "POST", "statusCode": 200, "contentType": "text/html", "params": []},
        ]
        result = _deduplicate_endpoints(endpoints)
        self.assertEqual(len(result), 2)

    @patch("scanners.crawler.requests.get")
    def test_crawl_basic(self, mock_get):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.headers = {"Content-Type": "text/html"}
        mock_response.text = """
        <html>
        <body>
            <a href="/about">About</a>
            <a href="/contact">Contact</a>
            <form action="/search" method="GET">
                <input name="q" type="text"/>
            </form>
        </body>
        </html>
        """
        mock_get.return_value = mock_response

        result = crawl("https://example.com", max_depth=1, timeout=5)

        self.assertGreater(len(result), 0)
        paths = [ep["path"] for ep in result]
        self.assertIn("/", paths)

    @patch("scanners.crawler.requests.get")
    def test_crawl_extracts_form_params(self, mock_get):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.headers = {"Content-Type": "text/html"}
        mock_response.text = """
        <html><body>
            <form action="/login" method="POST">
                <input name="username" type="text"/>
                <input name="password" type="password"/>
                <button type="submit">Login</button>
            </form>
        </body></html>
        """
        mock_get.return_value = mock_response

        result = crawl("https://example.com", max_depth=0, timeout=5)

        post_endpoints = [ep for ep in result if ep["method"] == "POST"]
        self.assertTrue(len(post_endpoints) > 0)
        params = post_endpoints[0]["params"]
        param_names = [p["name"] for p in params]
        self.assertIn("username", param_names)
        self.assertIn("password", param_names)

if __name__ == "__main__":
    unittest.main()
