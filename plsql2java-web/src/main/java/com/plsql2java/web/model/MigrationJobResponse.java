package com.plsql2java.web.model;

import com.plsql2java.orchestration.model.MigrationJobStatus;
import com.plsql2java.orchestration.model.MigrationProgress;

public class MigrationJobResponse {

    private final String jobId;
    private final MigrationJobStatus status;
    private final MigrationProgress progress;
    private final String errorMessage;

    public MigrationJobResponse(String jobId, MigrationJobStatus status,
                                 MigrationProgress progress, String errorMessage) {
        this.jobId = jobId;
        this.status = status;
        this.progress = progress;
        this.errorMessage = errorMessage;
    }

    public static MigrationJobResponse accepted(String jobId) {
        return new MigrationJobResponse(jobId, MigrationJobStatus.PENDING, null, null);
    }

    public String getJobId() { return jobId; }
    public MigrationJobStatus getStatus() { return status; }
    public MigrationProgress getProgress() { return progress; }
    public String getErrorMessage() { return errorMessage; }
}
