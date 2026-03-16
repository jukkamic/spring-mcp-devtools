package dev.scaffoldkit.mcp.tools;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
@Profile("dev")
public class ApplicationLogTool {
 
    private final ResourceLoader resourceLoader;

    public ApplicationLogTool(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @McpTool(description = "Returns the last x lines from the log file defined in log4j2-spring.xml. Default is 50 lines.")
    public String getRecentLogs(Integer lines) {
        // Set default value if not provided
        if (lines == null || lines <= 0) {
            lines = 50;
        }

        try {
            // Load log4j2-spring.xml to find the log file path
            Resource resource = resourceLoader.getResource("classpath:log4j2-spring.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(resource.getInputStream());
            document.getDocumentElement().normalize();

            // Extract LOG_FILE property
            NodeList propertiesList = document.getElementsByTagName("Properties");
            if (propertiesList.getLength() == 0) {
                return "Error: No Properties section found in log4j2-spring.xml";
            }

            Element properties = (Element) propertiesList.item(0);
            NodeList propertyNodes = properties.getElementsByTagName("Property");
            
            String logFilePath = null;
            for (int i = 0; i < propertyNodes.getLength(); i++) {
                Element prop = (Element) propertyNodes.item(i);
                String name = prop.getAttribute("name");
                if ("LOG_FILE".equals(name)) {
                    logFilePath = prop.getTextContent().trim();
                    break;
                }
            }

            if (logFilePath == null) {
                return "Error: LOG_FILE property not found in log4j2-spring.xml";
            }

            // Read the log file and get last N lines
            java.io.File logFile = new java.io.File(logFilePath);
            if (!logFile.exists()) {
                return String.format("Log file not found: %s", logFilePath);
            }

            java.nio.file.Path path = logFile.toPath();
            java.util.List<String> allLines = java.nio.file.Files.readAllLines(path);
            
            // Get the last N lines
            int startIndex = Math.max(0, allLines.size() - lines);
            java.util.List<String> recentLines = allLines.subList(startIndex, allLines.size());

            StringBuilder result = new StringBuilder();
            result.append(String.format("Last %d lines from log file: %s\n", recentLines.size(), logFilePath));
            result.append("=".repeat(80)).append("\n");
            
            for (String line : recentLines) {
                result.append(line).append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "Error reading log file: " + e.getMessage();
        }
    }
}