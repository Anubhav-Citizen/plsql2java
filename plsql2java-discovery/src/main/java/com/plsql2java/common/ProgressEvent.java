package com.plsql2java.common;

import java.time.Instant;

public class ProgressEvent {

    private final String migrationId;
    private final ProgressStage stage;
    private final String objectName;
    private final int processedCount;
    private final int totalCount;
    private final double percentComplete;
    private final String message;
    private final Instant timestamp;

    private ProgressEvent(Builder builder) {
        this.migrationId = builder.migrationId;
        this.stage = builder.stage;
        this.objectName = builder.objectName;
        this.processedCount = builder.processedCount;
        this.totalCount = builder.totalCount;
        this.percentComplete = builder.totalCount > 0
                ? (double) builder.processedCount / builder.totalCount * 100.0
                : -1.0;
        this.message = builder.message;
        this.timestamp = Instant.now();
    }

    public static Builder builder(String migrationId, ProgressStage stage) {
        return new Builder(migrationId, stage);
    }

    public String getMigrationId() { return migrationId; }
    public ProgressStage getStage() { return stage; }
    public String getObjectName() { return objectName; }
    public int getProcessedCount() { return processedCount; }
    public int getTotalCount() { return totalCount; }
    public double getPercentComplete() { return percentComplete; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }

    public static class Builder {
        private final String migrationId;
        private final ProgressStage stage;
        private String objectName;
        private int processedCount;
        private int totalCount;
        private String message = "";

        private Builder(String migrationId, ProgressStage stage) {
            this.migrationId = migrationId;
            this.stage = stage;
        }

        public Builder objectName(String objectName) { this.objectName = objectName; return this; }
        public Builder processed(int processed, int total) { this.processedCount = processed; this.totalCount = total; return this; }
        public Builder message(String message) { this.message = message; return this; }

        public ProgressEvent build() { return new ProgressEvent(this); }
    }
}
