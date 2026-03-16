### Configure Log File Discovery (Optional)

The ApplicationLogTool automatically discovers log files using multiple strategies. You can configure it via properties:

#### Option 1: Direct Configuration (Recommended)
```properties
scaffoldkit.mcp.log.file-path=logs/myapp.log
```

#### Option 2: Multiple Fallback Paths
```properties
scaffoldkit.mcp.log.fallback-paths[0]=logs/app.log
scaffoldkit.mcp.log.fallback-paths[1]=target/spring.log
scaffoldkit.mcp.log.fallback-paths[2]=logs/spring.log
```

#### Option 3: Auto-Discovery (Default)
No configuration needed - the system automatically searches for log files in:
- `logs/spring.log`, `logs/application.log`
- `spring.log`, `application.log`
- `target/spring.log`, `target/application.log`
- Framework-specific configs (Logback, Log4j2, Log4j1)

#### Alternative Configuration Methods
You can also set the log file path via:
- **System property**: `-Dlogging.file.name=/path/to/log.log` or `-DLOG_FILE=/path/to/log.log`
- **Environment variable**: `LOG_FILE=/path/to/log.log` or `SPRING_LOG_FILE=/path/to/log.log`

**Supported Logging Frameworks:**
- Logback (`logback.xml`, `logback-spring.xml`)
- Log4j2 (`log4j2.xml`, `log4j2-spring.xml`)
- Log4j 1.x (`log4j.properties`)
- Direct file paths
