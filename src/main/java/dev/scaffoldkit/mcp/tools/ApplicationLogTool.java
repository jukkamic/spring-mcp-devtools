package dev.scaffoldkit.mcp.tools;

import dev.scaffoldkit.mcp.config.McpProperties;
import dev.scaffoldkit.mcp.logging.LogDiscoveryService;
import dev.scaffoldkit.mcp.logging.parser.DirectPathParser;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * MCP tool for reading application logs.
 * Supports multiple logging frameworks through automatic discovery strategies.
 */
@Component
@Profile("dev")
public class ApplicationLogTool {

    private final LogDiscoveryService logDiscoveryService;
    private final DirectPathParser directPathParser;
    private final McpProperties mcpProperties;

    public ApplicationLogTool(LogDiscoveryService logDiscoveryService,
                             DirectPathParser directPathParser,
                             McpProperties mcpProperties) {
        this.logDiscoveryService = logDiscoveryService;
        this.directPathParser = directPathParser;
        this.mcpProperties = mcpProperties;
        
        // Configure direct path if specified in properties
        configureFromProperties();
    }

    /**
     * Configures the log discovery from application properties.
     */
    private void configureFromProperties() {
        McpProperties.Log logConfig = mcpProperties.getLog();
        
        // Set direct path if configured
        if (logConfig.getFilePath() != null && !logConfig.getFilePath().isEmpty()) {
            directPathParser.setDirectFilePath(logConfig.getFilePath());
        }
    }

    /**
     * Returns the last N lines from the application log file.
     * 
     * The log file is discovered using multiple strategies in priority order:
     * 1. Direct configuration (scaffoldkit.mcp.log.file-path)
     * 2. System properties (logging.file.name, LOG_FILE)
     * 3. Environment variables (LOG_FILE, SPRING_LOG_FILE)
     * 4. Common Spring Boot default locations
     * 5. Framework-specific configurations (Logback, Log4j2, Log4j1)
     * 6. Fallback paths from configuration
     *
     * @param lines the number of lines to retrieve (default: 50)
     * @return the last N lines from the log file
     */
    @McpTool(description = "Returns the last x lines from the application log file. The log file is automatically discovered or configured via scaffoldkit.mcp.log.file-path. Default is 50 lines.")
    public String getRecentLogs(Integer lines) {
        // Set default value if not provided
        if (lines == null || lines <= 0) {
            lines = 50;
        }

        try {
            // Discover log file path
            String logFilePath = discoverLogFilePath();
            
            if (logFilePath == null) {
                return buildErrorMessage();
            }

            // Read the log file and get last N lines
            File logFile = new File(logFilePath);
            Path path = logFile.toPath();
            List<String> allLines = Files.readAllLines(path);
            
            // Get the last N lines
            int startIndex = Math.max(0, allLines.size() - lines);
            List<String> recentLines = allLines.subList(startIndex, allLines.size());

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

    /**
     * Discovers the log file path using multiple strategies.
     * 
     * @return the discovered log file path, or null if not found
     */
    private String discoverLogFilePath() {
        // Try auto-discovery
        String logFilePath = logDiscoveryService.discoverLogFilePath();
        
        // If auto-discovery failed and we have fallback paths, try them
        if (logFilePath == null && !mcpProperties.getLog().getFallbackPaths().isEmpty()) {
            for (String fallbackPath : mcpProperties.getLog().getFallbackPaths()) {
                if (logDiscoveryService.isValidLogFile(fallbackPath)) {
                    logFilePath = fallbackPath;
                    break;
                }
            }
        }
        
        return logFilePath;
    }

    /**
     * Builds a helpful error message when no log file is found.
     * 
     * @return error message with configuration suggestions
     */
    private String buildErrorMessage() {
        StringBuilder error = new StringBuilder();
        error.append("Error: Could not discover log file path.\n");
        error.append("\n");
        error.append("Possible solutions:\n");
        error.append("1. Configure the log file path directly:\n");
        error.append("   scaffoldkit.mcp.log.file-path=/path/to/your/log/file.log\n");
        error.append("\n");
        error.append("2. Set a system property:\n");
        error.append("   -Dlogging.file.name=/path/to/your/log/file.log\n");
        error.append("\n");
        error.append("3. Set an environment variable:\n");
        error.append("   export LOG_FILE=/path/to/your/log/file.log\n");
        error.append("\n");
        error.append("4. Configure fallback paths:\n");
        error.append("   scaffoldkit.mcp.log.fallback-paths[0]=logs/app.log\n");
        error.append("   scaffoldkit.mcp.log.fallback-paths[1]=target/spring.log\n");
        error.append("\n");
        error.append("The system will automatically search for log files in these locations:\n");
        error.append("- logs/spring.log\n");
        error.append("- logs/application.log\n");
        error.append("- spring.log\n");
        error.append("- application.log\n");
        error.append("- target/spring.log\n");
        error.append("- target/application.log\n");
        error.append("\n");
        error.append("Supported logging frameworks:\n");
        error.append("- Logback (logback.xml, logback-spring.xml)\n");
        error.append("- Log4j2 (log4j2.xml, log4j2-spring.xml)\n");
        error.append("- Log4j 1.x (log4j.properties)\n");
        
        return error.toString();
    }
}
