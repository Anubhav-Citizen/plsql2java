package com.plsql2java.web.model;

import com.plsql2java.common.MigrationConfig;
import com.plsql2java.orchestration.model.AnalysisResult;
import com.plsql2java.orchestration.model.MigrationJobStatus;
import com.plsql2java.orchestration.model.MigrationProgress;
import com.plsql2java.orchestration.model.MigrationResult;

import java.nio.file.Path;
import java.time.Instant;

public class MigrationJobState {

    private final String jobId;
    private final MigrationConfig config;
    private volatile MigrationJobStatus status = MigrationJobStatus.PENDING;
    private volatile MigrationProgress latestProgress;
    private volatile AnalysisResult analysisResult;
    private volatile MigrationResult migrationResult;
    private volatile String errorMessage;
    private final Path outputDir;
    private final Instant startedAt = Instant.now();

    public MigrationJobState(String jobId, MigrationConfig config, Path outputDir) {
        this.jobId = jobId;
        this.config = config;
        this.outputDir = outputDir;
    }

    public String getJobId() { return jobId; }
    public MigrationConfig getConfig() { return config; }
    public MigrationJobStatus getStatus() { return status; }
    public void setStatus(MigrationJobStatus status) { this.status = status; }
    public MigrationProgress getLatestProgress() { return latestProgress; }
    public void setLatestProgress(MigrationProgress latestProgress) { this.latestProgress = latestProgress; }
    public AnalysisResult getAnalysisResult() { return analysisResult; }
    public void setAnalysisResult(AnalysisResult analysisResult) { this.analysisResult = analysisResult; }
    public MigrationResult getMigrationResult() { return migrationResult; }
    public void setMigrationResult(MigrationResult migrationResult) { this.migrationResult = migrationResult; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Path getOutputDir() { return outputDir; }
    public Instant getStartedAt() { return startedAt; }

    public boolean isComplete() {
        return status == MigrationJobStatus.COMPLETED || status == MigrationJobStatus.PARTIAL
                || status == MigrationJobStatus.FAILED;
    }
}
