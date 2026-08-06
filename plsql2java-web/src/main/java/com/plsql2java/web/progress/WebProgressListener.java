package com.plsql2java.web.progress;

import com.plsql2java.orchestration.event.ProgressEventBus.MigrationProgressListener;
import com.plsql2java.orchestration.model.MigrationProgress;

public class WebProgressListener implements MigrationProgressListener {

    private final String jobId;
    private final SseEmitterRegistry registry;

    public WebProgressListener(String jobId, SseEmitterRegistry registry) {
        this.jobId = jobId;
        this.registry = registry;
    }

    @Override
    public void onProgress(MigrationProgress progress) {
        registry.emit(jobId, "progress", new ProgressPayload(
                progress.getStage() != null ? progress.getStage().name() : "",
                progress.getPct(),
                progress.getMessage()));
    }

    private record ProgressPayload(String stage, int pct, String message) {}
}
