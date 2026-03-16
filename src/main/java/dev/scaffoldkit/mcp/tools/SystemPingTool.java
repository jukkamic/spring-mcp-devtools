package dev.scaffoldkit.mcp.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class SystemPingTool {

    private static final String VERSION = loadVersion();

    private static String loadVersion() {
        try (InputStream is = new ClassPathResource("version.properties").getInputStream()) {
            Properties props = new Properties();
            props.load(is);
            return props.getProperty("app.version", "unknown");
        } catch (IOException e) {
            return "unknown";
        }
    }

    @McpTool(description = "Ping the ScaffoldKit MCP bridge to verify it is active and responding.")
    public String pingScaffoldKit() {
        return "ScaffoldKit MCP DevTools (v" + VERSION + ") are active, loaded, and awaiting commands.";
    }
}
