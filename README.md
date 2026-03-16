# ScaffoldKit: MCP Spring DevTools 🏗️

ScaffoldKit is the scaffolding to your construction site, the softwar project. It incorproates an MCP server on your Spring-Boot application that enables your Agent to introspect your project via MCP tools.

In other words, ScaffoldKit is a custom Spring Boot Auto-Configuration library that bridges your Spring Boot applications with AI assistants (like Claude, Z.ai, and Cline) via the **Model Context Protocol (MCP)**. 

Instead of writing custom database inspectors or log tailers for every new project, just drop this dependency in, flip a property switch, and give your AI immediate, sandboxed access to your local development environment.

**Collaborators:** Check [README_COLLAB.MD](./README_COLLAB.md)

## 🚀 Quick Start

### 1. Install locally
Clone this repository and install it to your local Maven cache:

```bash
mvn clean install
```

### 2. Add to your project's Spring Boot application
In your target project's pom.xml, add the ScaffoldKit dependency:

```xml
<dependency>
    <groupId>dev.scaffoldkit</groupId>
    <artifactId>mcp-spring-devtools</artifactId>
</dependency>
```

### 3. Flip the Safety Switch
By default, ScaffoldKit is completely dormant to prevent accidental production data leaks.
To enable the tools, add this to your target application's application.properties (or rather application-dev.properties):

Properties
```properties
# Enable ScaffoldKit MCP tools
scaffoldkit.mcp.enabled=true    # Default false
scaffoldkit.mcp.port=9090       # Default 9090
```
(⚠️ WARNING: Never set this to true in a production environment!)

## Tips

Include the dependency inside a development profile so it won't be included in other deployments.