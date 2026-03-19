package dev.scaffoldkit.mcp.tools;

import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;

@Component
@Profile("dev")
class ApplicationLogTool {
    private final Environment env;

    ApplicationLogTool(Environment env) {
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

            String content = readLastLines(path, lines);
            if (content.startsWith("Error")) return content;

            // Count actual lines returned (handle empty content)
            int actualLines = content.isEmpty() ? 0 : (int) content.lines().count();

            StringBuilder sb = new StringBuilder();
            sb.append("Last ").append(actualLines).append(" lines from: ").append(logPath).append("\n");
            sb.append("=".repeat(80)).append("\n");
            sb.append(content);
            return sb.toString();
        } catch (Exception e) {
            return "Error reading log file: " + e.getMessage();
        }
    }

    /**
     * Reads the last N lines from a file efficiently by reading from the end.
     * Uses an iterative approach to read larger chunks until enough lines are found.
     */
    private String readLastLines(Path path, int lines) {
        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(path.toFile(), "r")) {
            long length = raf.length();
            if (length == 0) return "";

            // Start with a reasonable chunk size (estimated 200 bytes per line)
            int chunkSize = lines * 200;
            int minChunkSize = 4096; // At least 4KB
            chunkSize = Math.max(chunkSize, minChunkSize);
            
            // Cap maximum iterations to prevent infinite loops
            int maxIterations = 10;
            int iteration = 0;
            
            while (iteration < maxIterations) {
                iteration++;
                long startPointer = Math.max(0, length - chunkSize);
                raf.seek(startPointer);
                
                byte[] buffer = new byte[(int) (length - startPointer)];
                raf.readFully(buffer);
                String content = new String(buffer, java.nio.charset.StandardCharsets.UTF_8);
                
                // Count newlines to determine how many lines we have
                // Use simple newline counting for both \n and \r\n
                int newlineCount = 0;
                for (int i = 0; i < content.length(); i++) {
                    char c = content.charAt(i);
                    if (c == '\n') {
                        newlineCount++;
                    }
                }
                
                // If we started from position > 0, the first "line" is likely incomplete
                // Skip it to avoid partial lines by reducing the count
                int availableLines = newlineCount;
                if (startPointer > 0) {
                    availableLines--;
                }
                
                // Check if we have enough lines or we've read the whole file
                if (availableLines >= lines || startPointer == 0) {
                    // Split the content and extract the lines we need
                    java.util.List<String> linesList = new java.util.ArrayList<>();
                    String[] parts = content.split("\r?\n");
                    
                    // If we started from position > 0, skip the first element (partial line)
                    int startIndex = (startPointer > 0) ? 1 : 0;
                    
                    // Collect lines from the end
                    for (int i = parts.length - 1; i >= startIndex && linesList.size() < lines; i--) {
                        linesList.add(0, parts[i]);
                    }
                    
                    // Build result
                    StringBuilder sb = new StringBuilder();
                    for (String line : linesList) {
                        sb.append(line).append("\n");
                    }
                    return sb.toString();
                }
                
                // Need more data - double the chunk size
                chunkSize = chunkSize * 2;
                // Don't exceed file length
                chunkSize = (int) Math.min(chunkSize, length);
            }
            
            // Fallback: read the entire file if we couldn't get enough lines
            String allContent = Files.readString(path);
            String[] allLines = allContent.split("\r?\n");
            StringBuilder sb = new StringBuilder();
            int start = Math.max(0, allLines.length - lines);
            for (int i = start; i < allLines.length; i++) {
                sb.append(allLines[i]).append("\n");
            }
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
}
