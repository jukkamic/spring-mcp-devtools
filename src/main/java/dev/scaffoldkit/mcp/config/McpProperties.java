package dev.scaffoldkit.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scaffoldkit.mcp")
public class McpProperties {
    /**
     * * Enable or disable the ScaffoldKit MCP tools bridge.
     */
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}