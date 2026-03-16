package dev.scaffoldkit.mcp.logging;

import org.springframework.core.io.ResourceLoader;

/**
 * Interface for parsing logging configuration files to extract log file paths.
 * Implementations support different logging frameworks (Logback, Log4j2, etc.).
 */
public interface LogFileParser {

    /**
     * Determines if this parser can handle the given configuration file.
     *
     * @param configFileName the name of the configuration file
     * @return true if this parser supports the file type
     */
    boolean canParse(String configFileName);

    /**
     * Extracts the log file path from the logging configuration.
     *
     * @param resourceLoader the Spring resource loader for reading config files
     * @return the log file path, or null if not found
     */
    String extractLogFilePath(ResourceLoader resourceLoader);
}