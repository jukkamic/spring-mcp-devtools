package dev.scaffoldkit.mcp.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ConditionalOnProperty(prefix = "scaffoldkit.mcp", name = "enabled", havingValue = "true", matchIfMissing = false)
@ComponentScan(basePackages = "dev.scaffoldkit.mcp.tools")
public class McpDevToolsAutoConfiguration {
    // This class acts as the gateway. 
    // If the property is true, it scans the 'tools' package and loads your @McpTool beans.
    // If false or missing, this entire library stays completely dormant.
}