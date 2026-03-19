package dev.scaffoldkit.mcp.tools;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import dev.scaffoldkit.mcp.config.McpProperties;

import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Profile("dev")
class ApplicationLogTool {
    private final Environment env;
    private final McpProperties properties;

    ApplicationLogTool(McpProperties properties , Environment env) {
        this.properties = properties;
        this.env = env;
    }

    @McpTool(description = "Returns the last x lines from the application log file. Default is 50 lines.")
    String getRecentLogs(Integer lines) {
        if (lines == null || lines <= 0) lines = 50;
        String logPath = getLogFilePath();
        if (logPath == null) return "Log file not configured. Set logging.file.name property.";

        try {
            Path path = Path.of(logPath);
            if (!Files.exists(path)) return "Log file not found: " + logPath;

            long fileSize = Files.size(path);
            long maxSize = properties.getTools().getLogTailer().getMaxSize().toBytes();

            if(fileSize > maxSize) {
                String rawTail = readLastBytes(path, 102400); // Read last 100KB
                String[] allLines = rawTail.split("\\r?\\n");
                int start = Math.max(1, allLines.length - lines);
                StringBuilder sb = new StringBuilder();
                String note = String.format("[SYSTEM NOTE: Log file is %d MB and has been truncated. Displaying the last %d lines only.]\n", fileSize / (1024 * 1024));
                sb.append(note).append("=".repeat(80)).append("\n");
                for(int i = start; i<allLines.length; i++) {
                    sb.append(allLines[i]).append("\n");
                }
                return sb.toString();
            }
            
            var allLines = Files.readAllLines(path);
            int start = Math.max(0, allLines.size() - lines);
            var recentLines = allLines.subList(start, allLines.size());
            StringBuilder sb = new StringBuilder();
            sb.append("Last ").append(recentLines.size()).append(" lines from: ").append(logPath).append("\n");
            sb.append("=".repeat(80)).append("\n");
            recentLines.forEach(l -> sb.append(l).append("\n"));
            return sb.toString();
        } catch (Exception e) {
            return "Error reading log file: " + e.getMessage();
        }
    }

    private String getLogFilePath() {
        try {
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            for (ch.qos.logback.classic.Logger logger : ctx.getLoggerList()) {
                var it = logger.iteratorForAppenders();
                while (it.hasNext()) {
                    var appender = it.next();
                    if (appender instanceof FileAppender fileAppender) {
                        return fileAppender.getFile();
                    }
                }
            }
        } catch (Exception ignored) {}
        return env.getProperty("logging.file.name");
    }

    private String readLastBytes(Path path, int bytesToRead) {
        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(path.toFile(), "r")) {
            long length = raf.length();
            long startPointer = Math.max(0, length - bytesToRead);
            raf.seek(startPointer);
            byte[] buffer = new byte[(int) (length - startPointer)];
            raf.readFully(buffer);
            return new String(buffer, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Error reading log file: " + e.getMessage();
        }
    }
}
