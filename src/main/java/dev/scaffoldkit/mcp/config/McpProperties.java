package dev.scaffoldkit.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scaffoldkit.mcp")
public class McpProperties {
    /**
     * When true, enables the Model Context Protocol (MCP) server and
     * registers the devtools (Database Inspector, etc.) for AI assistants.
     */
    private boolean enabled = false;

    /**
     * The port used by the MCP SSE server.
     * Defaults to 9090 to avoid collisions with standard Spring Boot apps.
     */
    private int port = 9090;

    /**
     * Log configuration properties for the ApplicationLogTool.
     */
    private Log log = new Log();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Log configuration properties.
     */
    public static class Log {
        /**
         * Direct path to the log file. If specified, this overrides all auto-discovery strategies.
         */
        private String filePath;

        /**
         * Whether to enable automatic log file discovery.
         * When true, the system will attempt to find log files using multiple strategies.
         */
        private boolean autoDiscovery = true;

        /**
         * Additional fallback log file paths to check if auto-discovery fails.
         */
        private java.util.List<String> fallbackPaths = new java.util.ArrayList<>();

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public boolean isAutoDiscovery() {
            return autoDiscovery;
        }

        public void setAutoDiscovery(boolean autoDiscovery) {
            this.autoDiscovery = autoDiscovery;
        }

        public java.util.List<String> getFallbackPaths() {
            return fallbackPaths;
        }

        public void setFallbackPaths(java.util.List<String> fallbackPaths) {
            this.fallbackPaths = fallbackPaths;
        }
    }
}
