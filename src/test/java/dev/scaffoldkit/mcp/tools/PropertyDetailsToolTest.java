package dev.scaffoldkit.mcp.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * Unit tests for {@link PropertyDetailsTool}.
 * Uses Mockito to mock Spring Environment and PropertySource behavior.
 */
class PropertyDetailsToolTest {

    @Mock
    private ConfigurableEnvironment environment;

    @Mock
    private PropertySource<?> propertySource1;

    @Mock
    private PropertySource<?> propertySource2;

    @Mock
    private PropertySource<?> propertySource3;

    private PropertyDetailsTool propertyDetailsTool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        propertyDetailsTool = new PropertyDetailsTool(environment);
    }

    @Test
    @DisplayName("Should return error when property name is null")
    void propertyDetails_WhenPropertyNameIsNull_ReturnsErrorMessage() {
        // Act
        String result = propertyDetailsTool.propertyDetails(null);

        // Assert
        assertEquals("Error: Property name is required.", result);
        verifyNoInteractions(environment);
    }

    @Test
    @DisplayName("Should return error when property name is empty")
    void propertyDetails_WhenPropertyNameIsEmpty_ReturnsErrorMessage() {
        // Act
        String result = propertyDetailsTool.propertyDetails("");

        // Assert
        assertEquals("Error: Property name is required.", result);
        verifyNoInteractions(environment);
    }

    @Test
    @DisplayName("Should return error when property name is whitespace")
    void propertyDetails_WhenPropertyNameIsWhitespace_ReturnsErrorMessage() {
        // Act
        String result = propertyDetailsTool.propertyDetails("   ");

        // Assert
        assertEquals("Error: Property name is required.", result);
        verifyNoInteractions(environment);
    }

    @Test
    @DisplayName("Should return property not found message when property doesn't exist")
    void propertyDetails_WhenPropertyDoesNotExist_ReturnsNotFoundMessage() {
        // Arrange
        String propertyName = "nonexistent.property";
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(propertySource1);
        sources.addLast(propertySource2);
        
        when(environment.getPropertySources()).thenReturn(sources);
        when(environment.getProperty(propertyName)).thenReturn(null);
        when(propertySource1.getName()).thenReturn("source1");
        when(propertySource2.getName()).thenReturn("source2");
        when(propertySource1.getProperty(propertyName)).thenReturn(null);
        when(propertySource2.getProperty(propertyName)).thenReturn(null);

        // Act
        String result = propertyDetailsTool.propertyDetails(propertyName);

        // Assert
        assertTrue(result.contains("Property 'nonexistent.property' is not defined in any property source"));
        assertTrue(result.contains("Property Sources Checked:"));
        assertTrue(result.contains("- source1"));
        assertTrue(result.contains("- source2"));
        verify(environment, times(2)).getPropertySources();
        verify(environment).getProperty(propertyName);
        verify(propertySource1).getProperty(propertyName);
        verify(propertySource2).getProperty(propertyName);
    }

    @Test
    @DisplayName("Should return property details when property exists in single source")
    void propertyDetails_WhenPropertyExistsInSingleSource_ReturnsPropertyDetails() {
        // Arrange
        String propertyName = "app.name";
        String propertyValue = "MyApplication";
        String sourceName = "application.properties";
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(propertySource1);
        
        when(environment.getPropertySources()).thenReturn(sources);
        when(environment.getProperty(propertyName)).thenReturn(propertyValue);
        when(propertySource1.getName()).thenReturn(sourceName);
        when(propertySource1.getProperty(propertyName)).thenReturn(propertyValue);

        // Act
        String result = propertyDetailsTool.propertyDetails(propertyName);

        // Assert
        assertTrue(result.contains("Property: app.name"));
        assertTrue(result.contains("Resolved Value: MyApplication"));
        assertTrue(result.contains("Winning Source: application.properties"));
        assertTrue(result.contains("Property Sources (in precedence order - highest first):"));
        assertTrue(result.contains("application.properties: MyApplication ← WINNING"));
        assertTrue(result.contains("Property Source Hierarchy:"));
        assertTrue(result.contains("All Property Sources:"));
        assertTrue(result.contains("- application.properties"));
        verify(environment, times(2)).getPropertySources();
        verify(environment).getProperty(propertyName);
        verify(propertySource1).getProperty(propertyName);
    }

    @Test
    @DisplayName("Should return property details with multiple sources in precedence order")
    void propertyDetails_WhenPropertyExistsInMultipleSources_ReturnsPropertyDetailsInPrecedenceOrder() {
        // Arrange
        String propertyName = "app.version";
        String source1Name = "systemProperties";
        String source2Name = "application.properties";
        String source3Name = "defaultProperties";
        String value1 = "2.0.0";
        String value2 = "1.0.0";
        String value3 = "0.0.0";
        
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(propertySource1);
        sources.addLast(propertySource2);
        sources.addLast(propertySource3);
        
        when(environment.getPropertySources()).thenReturn(sources);
        when(environment.getProperty(propertyName)).thenReturn(value1);
        
        when(propertySource1.getName()).thenReturn(source1Name);
        when(propertySource1.getProperty(propertyName)).thenReturn(value1);
        
        when(propertySource2.getName()).thenReturn(source2Name);
        when(propertySource2.getProperty(propertyName)).thenReturn(value2);
        
        when(propertySource3.getName()).thenReturn(source3Name);
        when(propertySource3.getProperty(propertyName)).thenReturn(value3);

        // Act
        String result = propertyDetailsTool.propertyDetails(propertyName);

        // Assert
        assertTrue(result.contains("Property: app.version"));
        assertTrue(result.contains("Resolved Value: 2.0.0"));
        assertTrue(result.contains("Winning Source: systemProperties"));
        assertTrue(result.contains("systemProperties: 2.0.0 ← WINNING"));
        assertTrue(result.contains("application.properties: 1.0.0"));
        assertTrue(result.contains("defaultProperties: 0.0.0"));
        
        // Verify order: winning source appears first
        int winningIndex = result.indexOf("← WINNING");
        int source2Index = result.indexOf("application.properties: 1.0.0");
        int source3Index = result.indexOf("defaultProperties: 0.0.0");
        assertTrue(winningIndex < source2Index);
        assertTrue(source2Index < source3Index);
        
        verify(environment, times(2)).getPropertySources();
        verify(environment).getProperty(propertyName);
        verify(propertySource1).getProperty(propertyName);
        verify(propertySource2).getProperty(propertyName);
        verify(propertySource3).getProperty(propertyName);
    }

    @Test
    @DisplayName("Should handle property with null resolved value")
    void propertyDetails_WhenResolvedValueIsNull_ShouldDisplayNull() {
        // Arrange
        String propertyName = "nullable.property";
        String sourceName = "test.properties";
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(propertySource1);
        
        when(environment.getPropertySources()).thenReturn(sources);
        when(environment.getProperty(propertyName)).thenReturn(null);
        when(propertySource1.getName()).thenReturn(sourceName);
        when(propertySource1.getProperty(propertyName)).thenReturn(null);

        // Act
        String result = propertyDetailsTool.propertyDetails(propertyName);

        // Assert
        // If property value is null and found in sources, it would still be displayed
        // But the implementation checks if value != null before adding to propertySources
        // So this case would result in "not defined" message
        assertTrue(result.contains("Property 'nullable.property' is not defined in any property source"));
    }

    @Test
    @DisplayName("Should handle exception when property source doesn't support getProperty")
    void propertyDetails_WhenPropertySourceThrowsException_ShouldSkipSource() {
        // Arrange
        String propertyName = "tricky.property";
        String source2Name = "working.source";
        String propertyValue = "valid.value";
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(propertySource1);
        sources.addLast(propertySource2);
        
        when(environment.getPropertySources()).thenReturn(sources);
        when(environment.getProperty(propertyName)).thenReturn(propertyValue);
        
        when(propertySource1.getName()).thenReturn("broken.source");
        when(propertySource1.getProperty(propertyName)).thenThrow(new UnsupportedOperationException("Not supported"));
        
        when(propertySource2.getName()).thenReturn(source2Name);
        when(propertySource2.getProperty(propertyName)).thenReturn(propertyValue);

        // Act
        String result = propertyDetailsTool.propertyDetails(propertyName);

        // Assert
        assertTrue(result.contains("Property: tricky.property"));
        assertTrue(result.contains("Resolved Value: valid.value"));
        assertTrue(result.contains("Winning Source: working.source"));
        assertTrue(result.contains("working.source: valid.value ← WINNING"));
        // The broken source should not appear in the property sources list
        assertFalse(result.contains("broken.source:"));
        
        verify(environment, times(2)).getPropertySources();
        verify(environment).getProperty(propertyName);
        verify(propertySource1).getProperty(propertyName);
        verify(propertySource2).getProperty(propertyName);
    }

    @Test
    @DisplayName("Should include all property sources in hierarchy section")
    void propertyDetails_ShouldListAllPropertySourcesInHierarchy() {
        // Arrange
        String propertyName = "test.property";
        String propertyValue = "test.value";
        String source1Name = "commandLineArgs";
        String source2Name = "systemProperties";
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(propertySource1);
        sources.addLast(propertySource2);
        
        when(environment.getPropertySources()).thenReturn(sources);
        when(environment.getProperty(propertyName)).thenReturn(propertyValue);
        when(propertySource1.getName()).thenReturn(source1Name);
        when(propertySource1.getProperty(propertyName)).thenReturn(propertyValue);
        when(propertySource2.getName()).thenReturn(source2Name);
        when(propertySource2.getProperty(propertyName)).thenReturn(propertyValue);

        // Act
        String result = propertyDetailsTool.propertyDetails(propertyName);

        // Assert
        assertTrue(result.contains("All Property Sources:"));
        assertTrue(result.contains("- commandLineArgs"));
        assertTrue(result.contains("- systemProperties"));
        verify(environment, times(2)).getPropertySources();
    }

    @Test
    @DisplayName("Should include property source hierarchy explanation")
    void propertyDetails_ShouldIncludeHierarchyExplanation() {
        // Arrange
        String propertyName = "test.property";
        String propertyValue = "test.value";
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(propertySource1);
        
        when(environment.getPropertySources()).thenReturn(sources);
        when(environment.getProperty(propertyName)).thenReturn(propertyValue);
        when(propertySource1.getName()).thenReturn("test.source");
        when(propertySource1.getProperty(propertyName)).thenReturn(propertyValue);

        // Act
        String result = propertyDetailsTool.propertyDetails(propertyName);

        // Assert
        assertTrue(result.contains("Property Source Hierarchy:"));
        assertTrue(result.contains("1. Command line arguments (highest precedence)"));
        assertTrue(result.contains("2. System properties"));
        assertTrue(result.contains("3. Environment variables"));
        assertTrue(result.contains("4. Profile-specific configuration files"));
        assertTrue(result.contains("5. Application configuration files"));
        assertTrue(result.contains("6. @PropertySource annotations"));
        assertTrue(result.contains("7. Default properties (lowest precedence)"));
    }

    @Test
    @DisplayName("Should handle empty property source list")
    void propertyDetails_WhenNoPropertySourcesExist_ReturnsNotFoundMessage() {
        // Arrange
        String propertyName = "test.property";
        MutablePropertySources sources = new MutablePropertySources();
        
        when(environment.getPropertySources()).thenReturn(sources);
        when(environment.getProperty(propertyName)).thenReturn(null);

        // Act
        String result = propertyDetailsTool.propertyDetails(propertyName);

        // Assert
        assertTrue(result.contains("Property 'test.property' is not defined in any property source"));
        assertTrue(result.contains("Property Sources Checked:"));
        assertFalse(result.contains("- "));
        verify(environment, times(2)).getPropertySources();
        verify(environment).getProperty(propertyName);
    }

    @Test
    @DisplayName("Should handle property value with special characters")
    void propertyDetails_WhenPropertyValueHasSpecialCharacters_ShouldHandleCorrectly() {
        // Arrange
        String propertyName = "app.special.value";
        String propertyValue = "value with spaces & special@chars!";
        String sourceName = "config.properties";
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(propertySource1);
        
        when(environment.getPropertySources()).thenReturn(sources);
        when(environment.getProperty(propertyName)).thenReturn(propertyValue);
        when(propertySource1.getName()).thenReturn(sourceName);
        when(propertySource1.getProperty(propertyName)).thenReturn(propertyValue);

        // Act
        String result = propertyDetailsTool.propertyDetails(propertyName);

        // Assert
        assertTrue(result.contains("Property: app.special.value"));
        assertTrue(result.contains("Resolved Value: value with spaces & special@chars!"));
        assertTrue(result.contains("config.properties: value with spaces & special@chars! ← WINNING"));
        verify(environment, times(2)).getPropertySources();
        verify(environment).getProperty(propertyName);
        verify(propertySource1).getProperty(propertyName);
    }

    @Test
    @DisplayName("Should call environment getPropertySources twice")
    void propertyDetails_ShouldCallGetPropertySourcesTwice() {
        // Arrange
        String propertyName = "test.property";
        String propertyValue = "test.value";
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(propertySource1);
        
        when(environment.getPropertySources()).thenReturn(sources);
        when(environment.getProperty(propertyName)).thenReturn(propertyValue);
        when(propertySource1.getName()).thenReturn("test.source");
        when(propertySource1.getProperty(propertyName)).thenReturn(propertyValue);

        // Act
        propertyDetailsTool.propertyDetails(propertyName);

        // Assert
        // getPropertySources is called twice: once in propertyDetails() and once in listAllPropertySources()
        verify(environment, times(2)).getPropertySources();
        verify(environment, times(1)).getProperty(propertyName);
    }

    @Test
    @DisplayName("Should handle property in multiple sources with only first winning")
    void propertyDetails_WhenPropertyInMultipleSources_ShouldShowWinnerAndOthers() {
        // Arrange
        String propertyName = "multi.source.property";
        String value1 = "winner";
        String value2 = "loser";
        String value3 = "also-loser";
        
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(propertySource1);
        sources.addLast(propertySource2);
        sources.addLast(propertySource3);
        
        when(environment.getPropertySources()).thenReturn(sources);
        when(environment.getProperty(propertyName)).thenReturn(value1);
        
        when(propertySource1.getName()).thenReturn("source1");
        when(propertySource1.getProperty(propertyName)).thenReturn(value1);
        
        when(propertySource2.getName()).thenReturn("source2");
        when(propertySource2.getProperty(propertyName)).thenReturn(value2);
        
        when(propertySource3.getName()).thenReturn("source3");
        when(propertySource3.getProperty(propertyName)).thenReturn(value3);

        // Act
        String result = propertyDetailsTool.propertyDetails(propertyName);

        // Assert
        assertTrue(result.contains("Resolved Value: winner"));
        assertTrue(result.contains("Winning Source: source1"));
        
        // Only the first one should have ← WINNING
        int winCount = result.split("← WINNING", -1).length - 1;
        assertEquals(1, winCount);
        
        verify(propertySource1).getProperty(propertyName);
        verify(propertySource2).getProperty(propertyName);
        verify(propertySource3).getProperty(propertyName);
    }
}