package dev.scaffoldkit.mcp.tools;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import dev.scaffoldkit.mcp.config.McpProperties;

@Component
@Profile("dev")
class WebEndpointTool {
    private final RequestMappingHandlerMapping handlerMapping;
    private final McpProperties properties;

    WebEndpointTool(RequestMappingHandlerMapping handlerMapping, McpProperties properties) {
        this.handlerMapping = handlerMapping;
        this.properties = properties;
    }

    @McpTool(description = "Lists all registered HTTP endpoints int the application")
    String listEndpoints() {
        if(!properties.getTools().getWebEndpoints().isEnabled()) {
            return "Web endpoint tool is currently disabled.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s %-30s %s\n", "METHOD", "PATH", "HANDLER"));
        sb.append("=".repeat(80)).append("\n");

        handlerMapping.getHandlerMethods().forEach((info, method) -> {
            String methods = info.getMethodsCondition().getMethods().toString();
            String paths   = info.getDirectPaths().toString();
            String handler = method.getBeanType().getSimpleName() + "::" + method.getMethod().getName();

            sb.append(String.format("%-10s %-30s %s\n", methods, paths, handler));
        });
        return sb.toString();
    }
}
