package dev.scaffoldkit.mcp.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@EnableConfigurationProperties(McpProperties.class) // This line triggers the Processor!
@ConditionalOnProperty(prefix = "scaffoldkit.mcp", name = "enabled", havingValue = "true", matchIfMissing = false)
@ComponentScan(basePackages = "dev.scaffoldkit.mcp.tools")
public class McpDevToolsAutoConfiguration {
    // This class acts as the gateway.
    // If the property is true, it scans the 'tools' package and loads your @McpTool
    // beans.
    // If false or missing, this entire library stays completely dormant.
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> mcpPortCustomizer(McpProperties props) {
        return factory -> {
            // Create a brand new connector for our exotic port
            Connector mcpConnector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            mcpConnector.setPort(props.getPort()); // Defaults to 9090

            // Tell Tomcat to listen on this port in addition to the main one
            factory.addAdditionalConnectors(mcpConnector);
        };
    }
}