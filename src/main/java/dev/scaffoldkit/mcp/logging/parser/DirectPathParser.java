package dev.scaffoldkit.mcp.logging.parser;

import dev.scaffoldkit.mcp.logging.LogFileParser;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Parser for when a direct log file path is configured.
 * This is the highest priority parser when a file path is explicitly set.
 */
@Component
public class DirectPathParser implements LogFileParser {

    private String directFilePath;

    public void setDirectFilePath(String filePath) {
        this.directFilePath = filePath;
    }

    @Override
    public boolean canParse(String configFileName) {
        return directFilePath != null && !directFilePath.isEmpty();
    }

    @Override
    public String extractLogFilePath(ResourceLoader resourceLoader) {
        return directFilePath;
    }
}