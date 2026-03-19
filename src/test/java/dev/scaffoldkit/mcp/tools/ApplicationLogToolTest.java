package dev.scaffoldkit.mcp.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import dev.scaffoldkit.mcp.config.McpProperties;

/**
 * Unit tests for {@link ApplicationLogTool}.
 * Uses Mockito to mock Spring Environment and create temporary test files.
 */
class ApplicationLogToolTest {

    @Mock
    private Environment environment;

    private McpProperties properties;
    private ApplicationLogTool applicationLogTool;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        properties = new McpProperties();
        // Ensure the tools object is properly initialized
        properties.setTools(new McpProperties.Tools());
        applicationLogTool = new ApplicationLogTool(environment);
    }

    @Test
    @DisplayName("Should return default 50 lines when lines parameter is null")
    void getRecentLogs_WhenLinesIsNull_ReturnsDefaultLines() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.log");
        createTestFile(testFile, 100, "Line ");
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act
        String result = applicationLogTool.getRecentLogs(null);

        // Assert
        assertTrue(result.contains("Last 50 lines"));
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should return default 50 lines when lines parameter is zero")
    void getRecentLogs_WhenLinesIsZero_ReturnsDefaultLines() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.log");
        createTestFile(testFile, 100, "Line ");
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act
        String result = applicationLogTool.getRecentLogs(0);

        // Assert
        assertTrue(result.contains("Last 50 lines"));
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should return default 50 lines when lines parameter is negative")
    void getRecentLogs_WhenLinesIsNegative_ReturnsDefaultLines() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.log");
        createTestFile(testFile, 100, "Line ");
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act
        String result = applicationLogTool.getRecentLogs(-10);

        // Assert
        assertTrue(result.contains("Last 50 lines"));
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should return error message when log file is not configured")
    void getRecentLogs_WhenLogPathIsNull_ReturnsErrorMessage() {
        // Arrange
        when(environment.getProperty("logging.file.name")).thenReturn(null);

        // Act
        String result = applicationLogTool.getRecentLogs(10);

        // Assert
        assertEquals("Log file not configured. Set logging.file.name property.", result);
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should return error message when log file does not exist")
    void getRecentLogs_WhenLogFileDoesNotExist_ReturnsErrorMessage() {
        // Arrange
        String nonExistentPath = "/non/existent/path.log";
        when(environment.getProperty("logging.file.name")).thenReturn(nonExistentPath);

        // Act
        String result = applicationLogTool.getRecentLogs(10);

        // Assert
        assertEquals("Log file not found: " + nonExistentPath, result);
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should return requested number of lines from small file")
    void getRecentLogs_WhenFileIsSmall_ReturnsRequestedLines() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.log");
        createTestFile(testFile, 20, "Line ");
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act
        String result = applicationLogTool.getRecentLogs(10);

        // Assert
        assertTrue(result.contains("Last 10 lines"));
        assertTrue(result.contains("Line 11"));
        assertTrue(result.contains("Line 20"));
        assertFalse(result.contains("Line 10"));
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should return all lines when file has fewer lines than requested")
    void getRecentLogs_WhenFileHasFewerLines_ReturnsAllLines() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.log");
        createTestFile(testFile, 5, "Line ");
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act
        String result = applicationLogTool.getRecentLogs(10);

        // Assert
        assertTrue(result.contains("Last 5 lines"));
        assertTrue(result.contains("Line 1"));
        assertTrue(result.contains("Line 5"));
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should handle empty log file")
    void getRecentLogs_WhenFileIsEmpty_ReturnsEmptyContent() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.log");
        createTestFile(testFile, 0, "Line ");
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act
        String result = applicationLogTool.getRecentLogs(10);

        // Assert
        assertTrue(result.contains("Last 0 lines"));
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should return exactly requested lines from large file")
    void getRecentLogs_LargeFile_ReturnsExactlyRequestedLines() throws IOException {
        // Arrange - Test with different line counts
        Path testFile = tempDir.resolve("test.log");
        createTestFile(testFile, 500, "Line ");
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act - Request 25 lines
        String result = applicationLogTool.getRecentLogs(25);

        // Assert
        long logLineCount = result.lines()
            .filter(line -> line.startsWith("Line "))
            .count();
        
        assertEquals(25, logLineCount, "Should return exactly 25 lines from large file");
        
        // Verify the lines are from the end of the file
        assertTrue(result.contains("Line 476"));
        assertTrue(result.contains("Line 500"));
        assertFalse(result.contains("Line 1"));
        
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should include file path in output for small files")
    void getRecentLogs_WhenFileIsSmall_IncludesFilePath() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.log");
        createTestFile(testFile, 10, "Line ");
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act
        String result = applicationLogTool.getRecentLogs(5);

        // Assert
        assertTrue(result.contains("Last 5 lines from: " + testFile.toString()));
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should include separator line in output")
    void getRecentLogs_Always_IncludesSeparator() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.log");
        createTestFile(testFile, 10, "Line ");
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act
        String result = applicationLogTool.getRecentLogs(5);

        // Assert
        assertTrue(result.contains("=".repeat(80)));
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should handle lines parameter of 1")
    void getRecentLogs_WhenLinesIsOne_ReturnsSingleLine() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.log");
        createTestFile(testFile, 10, "Line ");
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act
        String result = applicationLogTool.getRecentLogs(1);

        // Assert
        assertTrue(result.contains("Last 1 lines"));
        assertTrue(result.contains("Line 10"));
        assertFalse(result.contains("Line 9"));
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should handle lines parameter larger than default")
    void getRecentLogs_WhenLinesIsLarge_ReturnsRequestedLines() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.log");
        createTestFile(testFile, 200, "Line ");
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act
        String result = applicationLogTool.getRecentLogs(100);

        // Assert
        assertTrue(result.contains("Last 100 lines"));
        assertTrue(result.contains("Line 101"));
        assertTrue(result.contains("Line 200"));
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should return error message when exception occurs reading file")
    void getRecentLogs_WhenExceptionOccurs_ReturnsErrorMessage() {
        // Arrange
        String nonExistentPath = "/non/existent/readonly/path.log";
        
        when(environment.getProperty("logging.file.name")).thenReturn(nonExistentPath);

        // Act
        String result = applicationLogTool.getRecentLogs(10);

        // Assert
        assertTrue(result.contains("Log file not found"));
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should handle file with lines containing special characters")
    void getRecentLogs_WhenLinesHaveSpecialCharacters_ReturnsCorrectContent() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.log");
        Files.writeString(testFile, "Line 1 with & special <chars>\nLine 2 with @ symbols\nLine 3 with # hash\n");
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act
        String result = applicationLogTool.getRecentLogs(3);

        // Assert
        assertTrue(result.contains("Line 1 with & special <chars>"));
        assertTrue(result.contains("Line 2 with @ symbols"));
        assertTrue(result.contains("Line 3 with # hash"));
        verify(environment).getProperty("logging.file.name");
    }

    @Test
    @DisplayName("Should handle file with mixed line endings")
    void getRecentLogs_WhenFileHasMixedLineEndings_ReturnsCorrectContent() throws IOException {
        // Arrange
        Path testFile = tempDir.resolve("test.log");
        // Mix of \n and \r\n line endings
        Files.writeString(testFile, "Line 1\nLine 2\r\nLine 3\nLine 4\r\n", StandardOpenOption.CREATE);
        
        when(environment.getProperty("logging.file.name")).thenReturn(testFile.toString());

        // Act
        String result = applicationLogTool.getRecentLogs(4);

        // Assert
        assertTrue(result.contains("Line 1"));
        assertTrue(result.contains("Line 2"));
        assertTrue(result.contains("Line 3"));
        assertTrue(result.contains("Line 4"));
        verify(environment).getProperty("logging.file.name");
    }

    /**
     * Helper method to create a test file with specified number of lines.
     */
    private void createTestFile(Path file, int lineCount, String linePrefix) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lineCount; i++) {
            sb.append(linePrefix).append(i).append("\n");
        }
        Files.writeString(file, sb.toString(), StandardOpenOption.CREATE);
    }
}