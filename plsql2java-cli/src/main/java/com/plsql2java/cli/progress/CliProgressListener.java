package com.plsql2java.cli.progress;

import com.plsql2java.orchestration.event.ProgressEventBus.MigrationProgressListener;
import com.plsql2java.orchestration.model.MigrationProgress;
import com.plsql2java.orchestration.model.PipelineStage;

import java.io.PrintStream;

public class CliProgressListener implements MigrationProgressListener {

    private final PrintStream out;
    private final boolean verbose;
    private PipelineStage lastStage;

    public CliProgressListener(PrintStream out, boolean verbose) {
        this.out = out;
        this.verbose = verbose;
    }

    @Override
    public void onProgress(MigrationProgress progress) {
        boolean isStageTransition = progress.getStage() != lastStage;
        if (isStageTransition) {
            lastStage = progress.getStage();
            out.printf("[PROGRESS] %s%n", formatStage(progress.getStage()));
        }
        if (verbose && progress.getMessage() != null) {
            if (progress.getObjectName() != null) {
                out.printf("[PROGRESS]   %s: %s (%d/%d)%n",
                        progress.getObjectName(), progress.getMessage(),
                        progress.getProcessed(), progress.getTotal());
            } else {
                out.printf("[PROGRESS]   %s%n", progress.getMessage());
            }
        }
    }

    private String formatStage(PipelineStage stage) {
        return stage.name().charAt(0) + stage.name().substring(1).toLowerCase().replace('_', ' ') + "...";
    }
}
