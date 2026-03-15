package dev.scaffoldkit.mcp.tools;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class SystemPingTool {

    @McpTool(description = "Ping the ScaffoldKit MCP bridge to verify it is active and responding.")
    public String pingScaffoldKit() {
        return "ScaffoldKit MCP DevTools (v0.0.1) are active, loaded, and awaiting commands.";
    }
}