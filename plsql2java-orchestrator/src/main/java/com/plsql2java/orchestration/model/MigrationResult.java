package com.plsql2java.orchestration.model;

import com.plsql2java.codegen.model.GeneratedProject;
import com.plsql2java.reporting.model.MigrationReport;
import com.plsql2java.scoring.model.ConfidenceReport;
import com.plsql2java.translation.model.TranslationResult;

import java.time.Instant;
import java.util.List;

public class MigrationResult {

    private final String migrationId;
    private final AnalysisResult analysisResult;
    private final List<TranslationResult> translationResults;
    private final GeneratedProject generatedProject;
    private final ConfidenceReport confidenceReport;
    private final MigrationReport migrationReport;
    private final List<String> skippedObjects;
    private final Instant completedAt;

    public MigrationResult(String migrationId, AnalysisResult analysisResult,
                            List<TranslationResult> translationResults,
                            GeneratedProject generatedProject,
                            ConfidenceReport confidenceReport,
                            MigrationReport migrationReport,
                            List<String> skippedObjects) {
        this.migrationId = migrationId;
        this.analysisResult = analysisResult;
        this.translationResults = translationResults;
        this.generatedProject = generatedProject;
        this.confidenceReport = confidenceReport;
        this.migrationReport = migrationReport;
        this.skippedObjects = skippedObjects;
        this.completedAt = Instant.now();
    }

    public boolean isPartial() { return !skippedObjects.isEmpty(); }

    public String getMigrationId() { return migrationId; }
    public AnalysisResult getAnalysisResult() { return analysisResult; }
    public List<TranslationResult> getTranslationResults() { return translationResults; }
    public GeneratedProject getGeneratedProject() { return generatedProject; }
    public ConfidenceReport getConfidenceReport() { return confidenceReport; }
    public MigrationReport getMigrationReport() { return migrationReport; }
    public List<String> getSkippedObjects() { return skippedObjects; }
    public Instant getCompletedAt() { return completedAt; }
}
