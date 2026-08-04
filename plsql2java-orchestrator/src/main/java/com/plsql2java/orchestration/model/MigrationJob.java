package com.plsql2java.orchestration.model;

import java.time.Instant;
import java.util.UUID;

public class MigrationJob {

    private final String jobId;
    private final String migrationId;
    private final OperationMode mode;
    private MigrationJobStatus status;
    private final Instant startedAt;
    private Instant completedAt;
    private String errorMessage;

    public MigrationJob(String migrationId, OperationMode mode) {
        this.jobId = UUID.randomUUID().toString();
        this.migrationId = migrationId;
        this.mode = mode;
        this.status = MigrationJobStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void complete() {
        this.status = MigrationJobStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void completePartial() {
        this.status = MigrationJobStatus.PARTIAL;
        this.completedAt = Instant.now();
    }

    public void fail(String errorMessage) {
        this.status = MigrationJobStatus.FAILED;
        this.completedAt = Instant.now();
        this.errorMessage = errorMessage;
    }

    public String getJobId() { return jobId; }
    public String getMigrationId() { return migrationId; }
    public OperationMode getMode() { return mode; }
    public MigrationJobStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getErrorMessage() { return errorMessage; }
}
