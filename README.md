# ScaffoldKit: MCP Spring DevTools 🏗️

[![Java CI with Maven](https://github.com/jukkamic/spring-mcp-devtools/actions/workflows/maven.yml/badge.svg)](https://github.com/jukkamic/spring-mcp-devtools/actions)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)

ScaffoldKit is the scaffolding to your construction site, the softwar project. It incorproates an MCP server on your Spring-Boot application and enables your Agent to introspect your project via MCP tools.

In other words, ScaffoldKit is a custom Spring Boot Auto-Configuration library that bridges your Spring Boot applications with AI assistants (like Claude, Z.ai, and Cline) via the **Model Context Protocol (MCP)**. 

Instead of writing custom database inspectors or log tailers for every new project, just drop this dependency in, flip a property switch, and give your AI immediate, sandboxed access to your local development environment.

**Collaborators:** Check [CONTRIBUTING.MD](./CONTRIBUTING.md)

## 🚀 Quick Start

## Setting the scaffolding up in your construction site

### 1. Install locally
Clone this repository and install it to your local Maven cache:

```bash
mvn clean install
```

### 2. Go to your target project and add the dependency
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

```properties
# Enable ScaffoldKit MCP tools
scaffoldkit.mcp.port=9090       # Default 9090
scaffoldkit.mcp.enabled=true    # Default false
```
(⚠️ WARNING: Never set this to true in a production environment!)

### 4. Cline MCP settings

In your target project's Cline panel's title bar click "MCP Servers" (small icon next to plus sign), select Configure tab and click Configure MCP Servers.

```json  
{
  "mcpServers": {
    "java-construction-site": {
      "disabled": false,
      "timeout": 60,
      "type": "sse",
      "url": "http://localhost:9090/sse"
    }
  }
}
```

### 5. Run
```bash
mvn clean compile
mvn dependency:copy-dependencies
mvn spring-boot:run
```
Make sure your MCP client is connected to: ```http://localhost:9090/mcp/sse``` and you're good to go!

"Hey robot, check the project's bean definitions"

## Tips
Include the dependency inside a development profile so it won't be included in other deployments.
