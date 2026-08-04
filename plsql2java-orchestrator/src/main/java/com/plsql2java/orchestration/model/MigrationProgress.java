package com.plsql2java.orchestration.model;

import java.time.Instant;

public class MigrationProgress {

    private final String migrationId;
    private final PipelineStage stage;
    private final String objectName;
    private final int processed;
    private final int total;
    private final int pct;
    private final String message;
    private final Instant timestamp;

    public MigrationProgress(String migrationId, PipelineStage stage, String objectName,
                              int processed, int total, String message) {
        this.migrationId = migrationId;
        this.stage = stage;
        this.objectName = objectName;
        this.processed = processed;
        this.total = total;
        this.pct = total > 0 ? Math.min(100, (processed * 100) / total) : 100;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public static MigrationProgress stageStart(String migrationId, PipelineStage stage) {
        return new MigrationProgress(migrationId, stage, null, 0, 0, "Starting " + stage.name().toLowerCase().replace('_', ' '));
    }

    public String getMigrationId() { return migrationId; }
    public PipelineStage getStage() { return stage; }
    public String getObjectName() { return objectName; }
    public int getProcessed() { return processed; }
    public int getTotal() { return total; }
    public int getPct() { return pct; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
}
