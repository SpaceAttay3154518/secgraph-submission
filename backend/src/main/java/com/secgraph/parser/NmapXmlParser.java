package com.secgraph.parser;

import com.secgraph.dto.ReconResult.DiscoveredTechnology;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NmapXmlParser {

    public static ParseResult parse(InputStream xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xml);

        List<DiscoveredTechnology> technologies = new ArrayList<>();
        List<String> openPorts = new ArrayList<>();

        NodeList hosts = doc.getElementsByTagName("host");
        for (int i = 0; i < hosts.getLength(); i++) {
            Element host = (Element) hosts.item(i);

            NodeList ports = host.getElementsByTagName("port");
            for (int j = 0; j < ports.getLength(); j++) {
                Element port = (Element) ports.item(j);
                String portId = port.getAttribute("portid");
                String protocol = port.getAttribute("protocol");

                NodeList states = port.getElementsByTagName("state");
                if (states.getLength() > 0) {
                    String state = ((Element) states.item(0)).getAttribute("state");
                    if (!"open".equals(state)) continue;
                }

                openPorts.add(protocol + "/" + portId);

                NodeList services = port.getElementsByTagName("service");
                if (services.getLength() > 0) {
                    Element service = (Element) services.item(0);
                    String product = service.getAttribute("product");
                    String version = service.getAttribute("version");
                    String name = service.getAttribute("name");

                    if (product != null && !product.isBlank()) {
                        DiscoveredTechnology tech = new DiscoveredTechnology();
                        tech.setName(product);
                        tech.setVersion(version != null && !version.isBlank() ? version : null);
                        tech.setCategory("server");
                        tech.setConfidence(0.85);
                        technologies.add(tech);
                    } else if (name != null && !name.isBlank()) {
                        DiscoveredTechnology tech = new DiscoveredTechnology();
                        tech.setName(name);
                        tech.setCategory("service");
                        tech.setConfidence(0.6);
                        technologies.add(tech);
                    }
                }
            }

            NodeList osMatches = host.getElementsByTagName("osmatch");
            for (int j = 0; j < osMatches.getLength() && j < 1; j++) {
                Element osMatch = (Element) osMatches.item(j);
                String osName = osMatch.getAttribute("name");
                if (osName != null && !osName.isBlank()) {
                    DiscoveredTechnology tech = new DiscoveredTechnology();
                    tech.setName(osName);
                    tech.setCategory("os");
                    String accuracy = osMatch.getAttribute("accuracy");
                    tech.setConfidence(accuracy != null ? Double.parseDouble(accuracy) / 100.0 : 0.5);
                    technologies.add(tech);
                }
            }
        }

        return new ParseResult(technologies, openPorts);
    }

    public static class ParseResult {
        private final List<DiscoveredTechnology> technologies;
        private final List<String> openPorts;

        public ParseResult(List<DiscoveredTechnology> technologies, List<String> openPorts) {
            this.technologies = technologies;
            this.openPorts = openPorts;
        }

        public List<DiscoveredTechnology> getTechnologies() { return technologies; }
        public List<String> getOpenPorts() { return openPorts; }
    }
}
