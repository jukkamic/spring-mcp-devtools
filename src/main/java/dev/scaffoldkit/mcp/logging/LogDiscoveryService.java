package dev.scaffoldkit.mcp.logging;

import dev.scaffoldkit.mcp.logging.parser.DirectPathParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for discovering log files using multiple strategies.
 * Implements a priority-based discovery mechanism to find log files
 * across different logging frameworks and configurations.
 */
@Service
public class LogDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(LogDiscoveryService.class);

    private final ResourceLoader resourceLoader;
    private final List<LogFileParser> parsers;
    private final DirectPathParser directPathParser;

    private String cachedLogPath;
    private long lastCacheTime = 0;
    private static final long CACHE_DURATION_MS = 60000; // Cache for 1 minute

    public LogDiscoveryService(ResourceLoader resourceLoader,
                               List<LogFileParser> parsers,
                               DirectPathParser directPathParser) {
        this.resourceLoader = resourceLoader;
        this.parsers = new ArrayList<>(parsers);
        this.directPathParser = directPathParser;
    }

    /**
     * Discovers the log file path using multiple strategies.
     * Priority order:
     * 1. Direct path configuration
     * 2. System properties (logging.file.name, LOG_FILE)
     * 3. Environment variables (LOG_FILE, SPRING_LOG_FILE)
     * 4. Common Spring Boot default locations
     * 5. Framework-specific configuration parsers (Logback, Log4j2, Log4j1)
     *
     * @return the discovered log file path, or null if not found
     */
    public String discoverLogFilePath() {
        // Return cached path if still valid
        if (cachedLogPath != null && System.currentTimeMillis() - lastCacheTime < CACHE_DURATION_MS) {
            return cachedLogPath;
        }

        // Clear cache
        cachedLogPath = null;
        lastCacheTime = System.currentTimeMillis();

        // Strategy 1: Direct path configuration (highest priority)
        if (directPathParser.canParse(null)) {
            String path = directPathParser.extractLogFilePath(resourceLoader);
            if (isValidLogFile(path)) {
                cachedLogPath = path;
                logger.debug("Found log file via direct path: {}", path);
                return path;
            }
        }

        // Strategy 2: System properties
        String systemPath = checkSystemProperties();
        if (systemPath != null && isValidLogFile(systemPath)) {
            cachedLogPath = systemPath;
            logger.debug("Found log file via system properties: {}", systemPath);
            return systemPath;
        }

        // Strategy 3: Environment variables
        String envPath = checkEnvironmentVariables();
        if (envPath != null && isValidLogFile(envPath)) {
            cachedLogPath = envPath;
            logger.debug("Found log file via environment variables: {}", envPath);
            return envPath;
        }

        // Strategy 4: Common Spring Boot default locations
        String defaultPath = checkDefaultLocations();
        if (defaultPath != null && isValidLogFile(defaultPath)) {
            cachedLogPath = defaultPath;
            logger.debug("Found log file via default location: {}", defaultPath);
            return defaultPath;
        }

        // Strategy 5: Framework-specific parsers
        String frameworkPath = checkFrameworkConfigs();
        if (frameworkPath != null && isValidLogFile(frameworkPath)) {
            cachedLogPath = frameworkPath;
            logger.debug("Found log file via framework config: {}", frameworkPath);
            return frameworkPath;
        }

        logger.warn("Could not discover log file path");
        return null;
    }

    /**
     * Validates that a log file path exists and is a regular file.
     *
     * @param path the file path to validate
     * @return true if the file exists and is readable
     */
    public boolean isValidLogFile(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File file = new File(path);
        return file.exists() && file.isFile() && file.canRead();
    }

    /**
     * Sets a direct log file path (overrides all discovery strategies).
     *
     * @param path the log file path
     */
    public void setDirectLogPath(String path) {
        directPathParser.setDirectFilePath(path);
        // Clear cache to force rediscovery
        cachedLogPath = null;
    }

    private String checkSystemProperties() {
        String path = System.getProperty("logging.file.name");
        if (path != null && !path.isEmpty()) {
            return path;
        }
        path = System.getProperty("LOG_FILE");
        if (path != null && !path.isEmpty()) {
            return path;
        }
        return null;
    }

    private String checkEnvironmentVariables() {
        String path = System.getenv("LOG_FILE");
        if (path != null && !path.isEmpty()) {
            return path;
        }
        path = System.getenv("SPRING_LOG_FILE");
        if (path != null && !path.isEmpty()) {
            return path;
        }
        return null;
    }

    private String checkDefaultLocations() {
        // Common Spring Boot default log file locations
        String[] defaultLocations = {
            "logs/spring.log",
            "logs/application.log",
            "spring.log",
            "application.log",
            "target/spring.log",
            "target/application.log"
        };

        for (String location : defaultLocations) {
            File file = new File(location);
            if (file.exists() && file.isFile() && file.canRead()) {
                return location;
            }
        }
        return null;
    }

    private String checkFrameworkConfigs() {
        for (LogFileParser parser : parsers) {
            if (parser instanceof DirectPathParser) {
                continue; // Skip direct path parser here
            }
            try {
                String path = parser.extractLogFilePath(resourceLoader);
                if (path != null && !path.isEmpty()) {
                    return path;
                }
            } catch (Exception e) {
                logger.debug("Parser {} failed: {}", parser.getClass().getSimpleName(), e.getMessage());
            }
        }
        return null;
    }
}