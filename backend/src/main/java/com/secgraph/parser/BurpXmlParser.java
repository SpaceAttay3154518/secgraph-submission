package com.secgraph.parser;

import com.secgraph.dto.ReconResult.DiscoveredEndpoint;
import com.secgraph.dto.ReconResult.DiscoveredParam;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BurpXmlParser {

    public static List<DiscoveredEndpoint> parse(InputStream xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xml);

        List<DiscoveredEndpoint> endpoints = new ArrayList<>();
        NodeList items = doc.getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);

            String url = getTagText(item, "url");
            String method = getTagText(item, "method");
            String statusStr = getTagText(item, "status");
            String mimeType = getTagText(item, "mimetype");

            if (url == null || url.isBlank()) continue;

            URI uri = new URI(url);
            String path = uri.getPath();
            if (path == null || path.isBlank()) path = "/";

            DiscoveredEndpoint ep = new DiscoveredEndpoint();
            ep.setPath(path);
            ep.setMethod(method != null ? method : "GET");
            ep.setContentType(mimeType);

            if (statusStr != null && !statusStr.isBlank()) {
                try { ep.setStatusCode(Integer.parseInt(statusStr)); } catch (NumberFormatException ignored) {}
            }

            List<DiscoveredParam> params = new ArrayList<>();
            String query = uri.getQuery();
            if (query != null) {
                for (String pair : query.split("&")) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length > 0 && !kv[0].isBlank()) {
                        DiscoveredParam p = new DiscoveredParam();
                        p.setName(URLDecoder.decode(kv[0], StandardCharsets.UTF_8));
                        p.setType("string");
                        p.setLocation("query");
                        params.add(p);
                    }
                }
            }

            if ("POST".equalsIgnoreCase(method)) {
                String requestBody = getRequestBody(item);
                if (requestBody != null) {
                    for (String pair : requestBody.split("&")) {
                        String[] kv = pair.split("=", 2);
                        if (kv.length > 0 && !kv[0].isBlank()) {
                            DiscoveredParam p = new DiscoveredParam();
                            p.setName(URLDecoder.decode(kv[0], StandardCharsets.UTF_8));
                            p.setType("string");
                            p.setLocation("body");
                            params.add(p);
                        }
                    }
                }
            }

            ep.setParams(params);
            endpoints.add(ep);
        }

        return endpoints;
    }

    private static String getTagText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return null;
        String text = nodes.item(0).getTextContent();
        return text != null ? text.trim() : null;
    }

    private static String getRequestBody(Element item) {
        NodeList requestNodes = item.getElementsByTagName("request");
        if (requestNodes.getLength() == 0) return null;

        Element requestEl = (Element) requestNodes.item(0);
        String base64 = requestEl.getAttribute("base64");
        String content = requestEl.getTextContent();

        if ("true".equals(base64) && content != null) {
            try {
                content = new String(Base64.getDecoder().decode(content.trim()), StandardCharsets.UTF_8);
            } catch (Exception e) {
                return null;
            }
        }

        if (content == null) return null;
        int bodyStart = content.indexOf("\r\n\r\n");
        if (bodyStart < 0) bodyStart = content.indexOf("\n\n");
        if (bodyStart < 0) return null;

        return content.substring(bodyStart).trim();
    }
}
