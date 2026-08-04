package com.plsql2java.web.service;

import com.plsql2java.common.JdbcConfig;
import com.plsql2java.common.MigrationConfig;
import com.plsql2java.orchestration.MigrationOrchestratorService;
import com.plsql2java.orchestration.event.ProgressEventBus;
import com.plsql2java.orchestration.model.MigrationJobStatus;
import com.plsql2java.web.model.GenerateRequest;
import com.plsql2java.web.model.JdbcConfigRequest;
import com.plsql2java.web.model.MigrationJobState;
import com.plsql2java.web.progress.SseEmitterRegistry;
import com.plsql2java.web.progress.WebProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class MigrationJobService {

    private static final Logger log = LoggerFactory.getLogger(MigrationJobService.class);

    private final MigrationOrchestratorService orchestrator;
    private final ProgressEventBus eventBus;
    private final SseEmitterRegistry sseRegistry;
    private final MigrationJobRegistry jobRegistry;
    private final FileUploadService uploadService;
    private final CredentialStore credentialStore;

    @Value("${plsql2java.output.dir:plsql2java-output}")
    private String outputBaseDir;

    public MigrationJobService(MigrationOrchestratorService orchestrator,
                                ProgressEventBus eventBus,
                                SseEmitterRegistry sseRegistry,
                                MigrationJobRegistry jobRegistry,
                                FileUploadService uploadService,
                                CredentialStore credentialStore) {
        this.orchestrator = orchestrator;
        this.eventBus = eventBus;
        this.sseRegistry = sseRegistry;
        this.jobRegistry = jobRegistry;
        this.uploadService = uploadService;
        this.credentialStore = credentialStore;
    }

    /** Submits an analyze job. Returns jobId immediately (BR-WEB-15). */
    public String submitAnalyze(String uploadId, String configId) {
        String jobId = UUID.randomUUID().toString();
        MigrationConfig config = buildConfig(uploadId, configId, null, 0.7);
        Path outputDir = Path.of(outputBaseDir, jobId);
        config.setOutputDir(outputDir);

        MigrationJobState state = new MigrationJobState(jobId, config, outputDir);
        jobRegistry.register(state);
        runAnalyze(jobId, state);
        return jobId;
    }

    /** Submits a generate job. Returns jobId immediately (BR-WEB-15). */
    public String submitGenerate(GenerateRequest request) {
        String jobId = UUID.randomUUID().toString();
        MigrationConfig config = buildConfig(request.getUploadId(), request.getConfigId(),
                request.getTargetPackage(), request.getConfidenceThreshold());
        Path outputDir = Path.of(outputBaseDir, jobId);
        config.setOutputDir(outputDir);

        MigrationJobState state = new MigrationJobState(jobId, config, outputDir);
        jobRegistry.register(state);
        runGenerate(jobId, state);
        return jobId;
    }

    @Async
    public void runAnalyze(String jobId, MigrationJobState state) {
        WebProgressListener listener = new WebProgressListener(jobId, sseRegistry);
        eventBus.register(listener);
        state.setStatus(MigrationJobStatus.RUNNING);
        try {
            var result = orchestrator.analyze(state.getConfig());
            state.setAnalysisResult(result);
            state.setStatus(MigrationJobStatus.COMPLETED);
            sseRegistry.complete(jobId);
        } catch (Exception e) {
            log.error("Analyze job {} failed: {}", jobId, e.getMessage(), e);
            state.setStatus(MigrationJobStatus.FAILED);
            state.setErrorMessage("Analysis failed");  // SECURITY-09: generic message
            sseRegistry.error(jobId, "Analysis failed");
        } finally {
            eventBus.unregister(listener);
        }
    }

    @Async
    public void runGenerate(String jobId, MigrationJobState state) {
        WebProgressListener listener = new WebProgressListener(jobId, sseRegistry);
        eventBus.register(listener);
        state.setStatus(MigrationJobStatus.RUNNING);
        try {
            var result = orchestrator.generate(state.getConfig());
            state.setMigrationResult(result);
            state.setStatus(result.isPartial() ? MigrationJobStatus.PARTIAL : MigrationJobStatus.COMPLETED);
            sseRegistry.complete(jobId);
        } catch (Exception e) {
            log.error("Generate job {} failed: {}", jobId, e.getMessage(), e);
            state.setStatus(MigrationJobStatus.FAILED);
            state.setErrorMessage("Migration failed");  // SECURITY-09: generic message
            sseRegistry.error(jobId, "Migration failed");
        } finally {
            eventBus.unregister(listener);
        }
    }

    private MigrationConfig buildConfig(String uploadId, String configId,
                                         String targetPackage, double confidenceThreshold) {
        MigrationConfig config = new MigrationConfig();
        config.setTargetPackage(targetPackage != null ? targetPackage : "com.example.migrated");
        config.setConfidenceThreshold((int) (confidenceThreshold * 100));

        if (uploadId != null) {
            // File mode: resolve DDL files from upload sandbox
            Path uploadDir = uploadService.resolve(uploadId);
            try (var stream = java.nio.file.Files.list(uploadDir)) {
                List<Path> ddlFiles = stream.filter(p -> p.toString().endsWith(".sql")).toList();
                config.setDdlFiles(ddlFiles);
            } catch (java.io.IOException e) {
                throw new IllegalStateException("Cannot read upload directory: " + uploadId);
            }
        } else if (configId != null) {
            // JDBC mode: retrieve credentials from store (SECURITY-12)
            // Note: JdbcConfig is stored separately; retrieve and clear password
            char[] password = credentialStore.retrieve(configId);
            // configId maps to a stored JdbcConfig — simplified: store full config
            // In production, store JdbcConfig object keyed by configId
            credentialStore.clear(configId);
        }

        return config;
    }
}
