package com.plsql2java.codegen.model;

import java.nio.file.Path;

public class GenerationContext {

    private final String migrationId;
    private final String targetPackage;
    private final String targetSpringBootVersion;
    private final Path outputDir;
    private final String dbDriver;
    private final int confidenceThreshold;
    private final String schemaName;
    private final String rawSource;

    public GenerationContext(String migrationId, String targetPackage, String targetSpringBootVersion,
                             Path outputDir, String dbDriver, int confidenceThreshold, String schemaName) {
        this(migrationId, targetPackage, targetSpringBootVersion, outputDir, dbDriver, confidenceThreshold, schemaName, "");
    }

    public GenerationContext(String migrationId, String targetPackage, String targetSpringBootVersion,
                             Path outputDir, String dbDriver, int confidenceThreshold, String schemaName, String rawSource) {
        this.migrationId = migrationId;
        this.targetPackage = targetPackage;
        this.targetSpringBootVersion = targetSpringBootVersion;
        this.outputDir = outputDir;
        this.dbDriver = dbDriver;
        this.confidenceThreshold = confidenceThreshold;
        this.schemaName = schemaName;
        this.rawSource = rawSource != null ? rawSource : "";
    }

    public String getMigrationId() { return migrationId; }
    public String getTargetPackage() { return targetPackage; }
    public String getTargetSpringBootVersion() { return targetSpringBootVersion; }
    public Path getOutputDir() { return outputDir; }
    public String getDbDriver() { return dbDriver; }
    public int getConfidenceThreshold() { return confidenceThreshold; }
    public String getSchemaName() { return schemaName; }
    public String getRawSource() { return rawSource; }
}
