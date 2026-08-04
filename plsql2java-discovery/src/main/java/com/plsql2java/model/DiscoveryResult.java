package com.plsql2java.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryResult {

    private String migrationId;
    private String schemaName;
    private DiscoveryMode discoveryMode;
    private List<OracleObject> objects = new ArrayList<>();
    private List<DiscoveryError> errors = new ArrayList<>();
    private List<String> sourceFiles = new ArrayList<>();
    private Instant discoveredAt;

    public DiscoveryResult() {}

    public DiscoveryResult(String migrationId, String schemaName, DiscoveryMode discoveryMode) {
        this.migrationId = migrationId;
        this.schemaName = schemaName;
        this.discoveryMode = discoveryMode;
        this.discoveredAt = Instant.now();
    }

    public String getMigrationId() { return migrationId; }
    public void setMigrationId(String migrationId) { this.migrationId = migrationId; }

    public String getSchemaName() { return schemaName; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }

    public DiscoveryMode getDiscoveryMode() { return discoveryMode; }
    public void setDiscoveryMode(DiscoveryMode discoveryMode) { this.discoveryMode = discoveryMode; }

    public List<OracleObject> getObjects() { return objects; }
    public void setObjects(List<OracleObject> objects) { this.objects = objects; }

    public List<DiscoveryError> getErrors() { return errors; }
    public void setErrors(List<DiscoveryError> errors) { this.errors = errors; }

    public List<String> getSourceFiles() { return sourceFiles; }
    public void setSourceFiles(List<String> sourceFiles) { this.sourceFiles = sourceFiles; }

    public Instant getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(Instant discoveredAt) { this.discoveredAt = discoveredAt; }

    public int getTotalObjectCount() { return objects.size(); }
    public int getErrorCount() { return errors.size(); }
    public long getObjectsWithCompilationErrors() {
        return objects.stream().filter(OracleObject::isHasCompilationErrors).count();
    }
}
