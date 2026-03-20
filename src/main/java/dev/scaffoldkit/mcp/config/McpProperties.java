package dev.scaffoldkit.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

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

    private Tools tools = new Tools();

    public static class Tools {
        private LogTailer logTailer = new LogTailer();
        private WebEndpoints webEndpoints = new WebEndpoints();
        public static class LogTailer {
            private DataSize maxSize = DataSize.ofMegabytes(2);

            public DataSize getMaxSize() {
                return maxSize;
            }
            public void setMaxSize(DataSize maxSize) {
                this.maxSize = maxSize;
            }
        }

        public static class WebEndpoints {
            private boolean enabled = true;
            public boolean isEnabled() {
                return enabled;
            }
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }

        public WebEndpoints getWebEndpoints() {
            return webEndpoints;
        }

        public void setWebEndpoints(WebEndpoints webEndpoints) {
            this.webEndpoints = webEndpoints;
        }

        public LogTailer getLogTailer() {
            return logTailer;
        }

        public void setLogTailer(LogTailer logTailer) {
            this.logTailer = logTailer;
        }
    }

    public Tools getTools() {
        return tools;
    }

    public void setTools(Tools tools) {
        this.tools = tools;
    }

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