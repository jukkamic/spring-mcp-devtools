package dev.scaffoldkit.mcp.tools;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class PropertyDetailsTool {

    private final ConfigurableEnvironment environment;

    public PropertyDetailsTool(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @McpTool(description = "Get detailed information about a Spring Boot property, including its resolved value and all sources that define it in the property hierarchy.")
    public String propertyDetails(String propertyName) {
        if (propertyName == null || propertyName.trim().isEmpty()) {
            return "Error: Property name is required.";
        }

        // Get the resolved value (the "winning" value)
        String resolvedValue = environment.getProperty(propertyName);
        
        // Collect all property sources that contain this property
        List<PropertySourceEntry> propertySources = new ArrayList<>();
        boolean found = false;
        
        for (PropertySource<?> source : environment.getPropertySources()) {
            try {
                Object value = source.getProperty(propertyName);
                if (value != null) {
                    propertySources.add(new PropertySourceEntry(source.getName(), value.toString()));
                    found = true;
                }
            } catch (Exception e) {
                // Some property sources may not support getProperty, skip them
                continue;
            }
        }

        if (!found) {
            return String.format("Property '%s' is not defined in any property source.\n\n", propertyName) +
                   "Property Sources Checked:\n" +
                   "======================\n" +
                   listAllPropertySources();
        }

        StringBuilder result = new StringBuilder();
        result.append("Property: ").append(propertyName).append("\n");
        result.append("Resolved Value: ").append(resolvedValue != null ? resolvedValue : "<null>").append("\n");
        
        // The first property source in the list is the "winning" one (highest precedence)
        String winningSource = propertySources.get(0).name;
        result.append("Winning Source: ").append(winningSource).append("\n");
        result.append("\n");
        
        result.append("Property Sources (in precedence order - highest first):\n");
        result.append("======================================================\n");
        
        for (int i = 0; i < propertySources.size(); i++) {
            PropertySourceEntry entry = propertySources.get(i);
            result.append(entry.name);
            result.append(": ");
            result.append(entry.value);
            
            if (i == 0) {
                result.append(" ← WINNING");
            }
            result.append("\n");
        }
        
        result.append("\n");
        result.append("Property Source Hierarchy:\n");
        result.append("==========================\n");
        result.append("1. Command line arguments (highest precedence)\n");
        result.append("2. System properties\n");
        result.append("3. Environment variables\n");
        result.append("4. Profile-specific configuration files\n");
        result.append("5. Application configuration files\n");
        result.append("6. @PropertySource annotations\n");
        result.append("7. Default properties (lowest precedence)\n");
        
        result.append("\n");
        result.append("All Property Sources:\n");
        result.append("=====================\n");
        result.append(listAllPropertySources());
        
        return result.toString();
    }

    private String listAllPropertySources() {
        StringBuilder sb = new StringBuilder();
        for (PropertySource<?> source : environment.getPropertySources()) {
            sb.append("- ").append(source.getName()).append("\n");
        }
        return sb.toString();
    }

    private static class PropertySourceEntry {
        final String name;
        final String value;

        PropertySourceEntry(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}