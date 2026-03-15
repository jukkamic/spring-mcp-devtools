package dev.scaffoldkit.mcp.tools;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
@Profile("dev")

public class ProjectDiagnosticsTool {
 
    private final ApplicationContext context;
    private final ResourceLoader resourceLoader;

    public ProjectDiagnosticsTool(ApplicationContext context, ResourceLoader resourceLoader) {
        this.context = context;
        this.resourceLoader = resourceLoader;
    }

    @McpTool(description = "Returns our custom Spring Bean names for the construction ledger")
    public List<String> listProjectBeans() {
        return Arrays.stream(context.getBeanDefinitionNames())
                // Filter for your package to hide the Spring infrastructure noise
                .filter(name -> name.contains("fi.kotkis") ||
                        !name.contains(".")) // Also show short names like "materialController"
                .sorted()
                .collect(Collectors.toList());
    }

    @McpTool(description = "Returns the dependencies listed in pom.xml")
    public String listPomDependencies() {
        try {
            // Load pom.xml from the project root using ResourceLoader
            Resource resource = resourceLoader.getResource("file:./pom.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(resource.getInputStream());
            document.getDocumentElement().normalize();

            StringBuilder result = new StringBuilder();
            result.append("Project Dependencies from pom.xml:\n");
            result.append("================================\n\n");

            // Extract parent version
            NodeList parents = document.getElementsByTagName("parent");
            if (parents.getLength() > 0) {
                Element parent = (Element) parents.item(0);
                String parentGroupId = getElementText(parent, "groupId");
                String parentArtifactId = getElementText(parent, "artifactId");
                String parentVersion = getElementText(parent, "version");
                
                result.append("Parent:\n");
                result.append("=======\n");
                result.append(String.format("Group ID: %s\n", parentGroupId));
                result.append(String.format("Artifact ID: %s\n", parentArtifactId));
                result.append(String.format("Version: %s\n\n", parentVersion));
            }

            // Extract version properties
            NodeList propertiesList = document.getElementsByTagName("properties");
            if (propertiesList.getLength() > 0) {
                Element properties = (Element) propertiesList.item(0);
                NodeList propertyNodes = properties.getChildNodes();
                
                result.append("Version Properties:\n");
                result.append("==================\n");
                
                for (int i = 0; i < propertyNodes.getLength(); i++) {
                    if (propertyNodes.item(i) instanceof Element) {
                        Element prop = (Element) propertyNodes.item(i);
                        String propName = prop.getTagName();
                        // Only show properties ending with ".version"
                        if (propName.endsWith(".version")) {
                            String propValue = prop.getTextContent().trim();
                            result.append(String.format("- %s: %s\n", propName, propValue));
                        }
                    }
                }
                result.append("\n");
            }

            result.append("Dependencies:\n");
            result.append("=============\n");

            // Get dependencies from main dependencies section
            NodeList dependencies = document.getElementsByTagName("dependency");
            
            for (int i = 0; i < dependencies.getLength(); i++) {
                Element dep = (Element) dependencies.item(i);
                String groupId = getElementText(dep, "groupId");
                String artifactId = getElementText(dep, "artifactId");
                String version = getElementText(dep, "version");
                String scope = getElementText(dep, "scope");

                result.append(String.format("- %s:%s", groupId, artifactId));
                if (version != null && !version.isEmpty()) {
                    result.append(String.format(":%s", version));
                }
                if (scope != null && !scope.isEmpty()) {
                    result.append(String.format(" (scope: %s)", scope));
                }
                result.append("\n");
            }

            // Check for dependencyManagement section
            NodeList depManagementList = document.getElementsByTagName("dependencyManagement");
            if (depManagementList.getLength() > 0) {
                result.append("\nDependency Management:\n");
                result.append("=====================\n");
                Element depManagement = (Element) depManagementList.item(0);
                NodeList managedDeps = depManagement.getElementsByTagName("dependency");
                
                for (int i = 0; i < managedDeps.getLength(); i++) {
                    Element dep = (Element) managedDeps.item(i);
                    String groupId = getElementText(dep, "groupId");
                    String artifactId = getElementText(dep, "artifactId");
                    String version = getElementText(dep, "version");

                    result.append(String.format("- %s:%s:%s\n", groupId, artifactId, version));
                }
            }

            return result.toString();
        } catch (Exception e) {
            return "Error reading pom.xml: " + e.getMessage();
        }
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
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
