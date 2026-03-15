package dev.scaffoldkit.mcp.tools;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInspectorTool {

    private final DataSource dataSource;

    // Spring automatically injects the host application's database connection here!
    public DatabaseInspectorTool(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @McpTool(description = "Get the database schema, listing all tables and their columns. Use this to understand the data structure.")
    public String getDatabaseSchema() {
        StringBuilder schema = new StringBuilder("Database Schema (User Tables Only):\n\n");

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // Get all tables
            ResultSet tables = metaData.getTables(null, null, "%", new String[] { "TABLE" });
            while (tables.next()) {
                String schemaName = tables.getString("TABLE_SCHEM");

                // Skip the noisy internal engine tables
                if (schemaName != null && (schemaName.equalsIgnoreCase("INFORMATION_SCHEMA") ||
                        schemaName.equalsIgnoreCase("pg_catalog") ||
                        schemaName.equalsIgnoreCase("SYS"))) {
                    continue;
                }

                String tableName = tables.getString("TABLE_NAME");
                schema.append("Table: ").append(tableName).append("\n");

                // Get columns for each table
                ResultSet columns = metaData.getColumns(null, null, tableName, "%");
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    schema.append("  - ").append(columnName)
                            .append(" (").append(columnType).append("(").append(columnSize).append("))\n");
                }
                schema.append("\n");
            }
        } catch (Exception e) {
            return "Error reading schema: " + e.getMessage();
        }

        return schema.toString();
    }
}