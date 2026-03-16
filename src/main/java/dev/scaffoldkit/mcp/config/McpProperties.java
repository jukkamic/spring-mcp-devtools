package dev.scaffoldkit.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scaffoldkit.mcp")
public class McpProperties {
    /**
     * When true, enables the Model Context Protocol (MCP) server and
     * registers the devtools (Database Inspector, etc.) for AI assistants.
     */
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}