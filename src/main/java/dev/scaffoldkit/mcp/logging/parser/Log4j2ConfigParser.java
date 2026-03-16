package dev.scaffoldkit.mcp.logging.parser;

import dev.scaffoldkit.mcp.logging.LogFileParser;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

/**
 * Parser for Log4j2 configuration files (log4j2-spring.xml, log4j2.xml).
 * Extracts log file path from LOG_FILE property in XML configuration.
 */
@Component
public class Log4j2ConfigParser implements LogFileParser {

    private static final String[] SUPPORTED_CONFIGS = {
        "log4j2-spring.xml",
        "log4j2.xml"
    };

    @Override
    public boolean canParse(String configFileName) {
        for (String supported : SUPPORTED_CONFIGS) {
            if (supported.equals(configFileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String extractLogFilePath(ResourceLoader resourceLoader) {
        for (String configFile : SUPPORTED_CONFIGS) {
            try {
                Resource resource = resourceLoader.getResource("classpath:" + configFile);
                if (resource.exists()) {
                    return parseLog4j2Config(resource);
                }
            } catch (Exception e) {
                // Try next config file
            }
        }
        return null;
    }

    private String parseLog4j2Config(Resource resource) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document;

        try (InputStream inputStream = resource.getInputStream()) {
            document = builder.parse(inputStream);
        }
        document.getDocumentElement().normalize();

        // Extract LOG_FILE property
        NodeList propertiesList = document.getElementsByTagName("Properties");
        if (propertiesList.getLength() == 0) {
            return null;
        }

        Element properties = (Element) propertiesList.item(0);
        NodeList propertyNodes = properties.getElementsByTagName("Property");

        for (int i = 0; i < propertyNodes.getLength(); i++) {
            Element prop = (Element) propertyNodes.item(i);
            String name = prop.getAttribute("name");
            if ("LOG_FILE".equals(name)) {
                return prop.getTextContent().trim();
            }
        }

        return null;
    }
}