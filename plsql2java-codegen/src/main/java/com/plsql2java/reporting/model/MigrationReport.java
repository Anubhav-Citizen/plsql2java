package com.plsql2java.reporting.model;

import java.time.Instant;

public class MigrationReport {

    private final String migrationId;
    private final String markdownContent;
    private final String htmlContent;
    private final Instant generatedAt;
    private final String schemaName;

    public MigrationReport(String migrationId, String markdownContent, String htmlContent, String schemaName) {
        this.migrationId = migrationId;
        this.markdownContent = markdownContent;
        this.htmlContent = htmlContent;
        this.schemaName = schemaName;
        this.generatedAt = Instant.now();
    }

    public String getMigrationId() { return migrationId; }
    public String getMarkdownContent() { return markdownContent; }
    public String getHtmlContent() { return htmlContent; }
    public Instant getGeneratedAt() { return generatedAt; }
    public String getSchemaName() { return schemaName; }
}
