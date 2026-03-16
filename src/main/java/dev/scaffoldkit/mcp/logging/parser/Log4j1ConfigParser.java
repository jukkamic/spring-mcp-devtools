package dev.scaffoldkit.mcp.logging.parser;

import dev.scaffoldkit.mcp.logging.LogFileParser;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Parser for Log4j 1.x configuration files (log4j.properties).
 * Extracts log file path from file appender configurations.
 */
@Component
public class Log4j1ConfigParser implements LogFileParser {

    private static final String[] SUPPORTED_CONFIGS = {
        "log4j.properties"
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
                    return parseLog4j1Config(resource);
                }
            } catch (Exception e) {
                // Try next config file
            }
        }
        return null;
    }

    private String parseLog4j1Config(Resource resource) throws Exception {
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Look for file appender configuration
                // Pattern: log4j.appender.<name>.File=<path>
                if (line.startsWith("log4j.appender.") && line.contains(".File=")) {
                    int equalsIndex = line.indexOf("=");
                    if (equalsIndex > 0) {
                        return line.substring(equalsIndex + 1).trim();
                    }
                }
            }
        }

        return null;
    }
}