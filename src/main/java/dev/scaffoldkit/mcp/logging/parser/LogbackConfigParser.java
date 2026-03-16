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
 * Parser for Logback configuration files (logback.xml, logback-spring.xml).
 * Extracts log file path from <file> tags in appender configurations.
 */
@Component
public class LogbackConfigParser implements LogFileParser {

    private static final String[] SUPPORTED_CONFIGS = {
        "logback-spring.xml",
        "logback.xml"
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
                    return parseLogbackConfig(resource);
                }
            } catch (Exception e) {
                // Try next config file
            }
        }
        return null;
    }

    private String parseLogbackConfig(Resource resource) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document;

        try (InputStream inputStream = resource.getInputStream()) {
            document = builder.parse(inputStream);
        }
        document.getDocumentElement().normalize();

        // Look for <appender> elements with <file> child
        NodeList appenderList = document.getElementsByTagName("appender");
        for (int i = 0; i < appenderList.getLength(); i++) {
            Element appender = (Element) appenderList.item(i);
            
            // Check if it's a file appender (FileAppender, RollingFileAppender, etc.)
            String appenderClass = appender.getAttribute("class");
            if (appenderClass != null && 
                (appenderClass.contains("FileAppender") || appenderClass.contains("RollingFileAppender"))) {
                
                NodeList fileNodes = appender.getElementsByTagName("file");
                if (fileNodes.getLength() > 0) {
                    return fileNodes.item(0).getTextContent().trim();
                }
            } else {
                // Also try direct <file> tag without class check
                NodeList fileNodes = appender.getElementsByTagName("file");
                if (fileNodes.getLength() > 0) {
                    return fileNodes.item(0).getTextContent().trim();
                }
            }
        }

        return null;
    }
}