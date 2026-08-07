package com.plsql2java.web.service;

import com.plsql2java.orchestration.MigrationOrchestratorService;
import com.plsql2java.orchestration.event.ProgressEventBus;
import com.plsql2java.orchestration.model.MigrationJobStatus;
import com.plsql2java.web.model.MigrationJobState;
import com.plsql2java.web.progress.SseEmitterRegistry;
import com.plsql2java.web.progress.WebProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Separate bean so @Async proxy is honoured — self-invocation within
 * MigrationJobService would bypass the CGLIB proxy and run synchronously.
 */
@Service
public class MigrationJobRunner {

    private static final Logger log = LoggerFactory.getLogger(MigrationJobRunner.class);

    private final MigrationOrchestratorService orchestrator;
    private final ProgressEventBus eventBus;
    private final SseEmitterRegistry sseRegistry;

    public MigrationJobRunner(MigrationOrchestratorService orchestrator,
                               ProgressEventBus eventBus,
                               SseEmitterRegistry sseRegistry) {
        this.orchestrator = orchestrator;
        this.eventBus = eventBus;
        this.sseRegistry = sseRegistry;
    }

    @Async("migrationTaskExecutor")
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
            state.setErrorMessage("Analysis failed");
            sseRegistry.error(jobId, "Analysis failed");
        } finally {
            eventBus.unregister(listener);
        }
    }

    @Async("migrationTaskExecutor")
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
            state.setErrorMessage("Migration failed");
            sseRegistry.error(jobId, "Migration failed");
        } finally {
            eventBus.unregister(listener);
        }
    }
}
