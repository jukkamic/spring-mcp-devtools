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
}