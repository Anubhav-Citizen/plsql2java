package com.plsql2java.reporting.model;

import com.plsql2java.codegen.model.GeneratedProject;
import com.plsql2java.common.MigrationConfig;
import com.plsql2java.model.DependencyGraph;
import com.plsql2java.model.DiscoveryResult;
import com.plsql2java.scoring.model.ConfidenceReport;
import com.plsql2java.translation.model.TranslationResult;

import java.util.List;

public class ReportInput {

    private final DiscoveryResult discoveryResult;
    private final DependencyGraph dependencyGraph;
    private final List<TranslationResult> translationResults;
    private final GeneratedProject generatedProject;
    private final ConfidenceReport confidenceReport;
    private final MigrationConfig migrationConfig;

    public ReportInput(DiscoveryResult discoveryResult, DependencyGraph dependencyGraph,
                       List<TranslationResult> translationResults, GeneratedProject generatedProject,
                       ConfidenceReport confidenceReport, MigrationConfig migrationConfig) {
        this.discoveryResult = discoveryResult;
        this.dependencyGraph = dependencyGraph;
        this.translationResults = translationResults;
        this.generatedProject = generatedProject;
        this.confidenceReport = confidenceReport;
        this.migrationConfig = migrationConfig;
    }

    public DiscoveryResult getDiscoveryResult() { return discoveryResult; }
    public DependencyGraph getDependencyGraph() { return dependencyGraph; }
    public List<TranslationResult> getTranslationResults() { return translationResults; }
    public GeneratedProject getGeneratedProject() { return generatedProject; }
    public ConfidenceReport getConfidenceReport() { return confidenceReport; }
    public MigrationConfig getMigrationConfig() { return migrationConfig; }
}
