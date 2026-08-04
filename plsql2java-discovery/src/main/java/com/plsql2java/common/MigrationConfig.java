package com.plsql2java.common;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MigrationConfig {

    private String migrationId = UUID.randomUUID().toString();
    private JdbcConfig jdbcConfig;
    private List<Path> ddlFiles = new ArrayList<>();
    private Path outputDir;
    private int confidenceThreshold = 70;
    private String schemaName;
    private String targetPackage;

    public MigrationConfig() {}

    public String getMigrationId() { return migrationId; }
    public void setMigrationId(String migrationId) { this.migrationId = migrationId; }

    public JdbcConfig getJdbcConfig() { return jdbcConfig; }
    public void setJdbcConfig(JdbcConfig jdbcConfig) { this.jdbcConfig = jdbcConfig; }

    public List<Path> getDdlFiles() { return ddlFiles; }
    public void setDdlFiles(List<Path> ddlFiles) { this.ddlFiles = ddlFiles; }

    public Path getOutputDir() { return outputDir; }
    public void setOutputDir(Path outputDir) { this.outputDir = outputDir; }

    public int getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(int confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }

    public String getSchemaName() { return schemaName; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }

    public String getTargetPackage() { return targetPackage; }
    public void setTargetPackage(String targetPackage) { this.targetPackage = targetPackage; }

    public boolean isJdbcMode() { return jdbcConfig != null; }
    public boolean isFileMode() { return !ddlFiles.isEmpty(); }
}
