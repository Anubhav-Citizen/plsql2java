package com.plsql2java.orchestration.model;

import com.plsql2java.model.DependencyGraph;
import com.plsql2java.model.DiscoveryResult;

import java.time.Instant;

public class AnalysisResult {

    private final String migrationId;
    private final DiscoveryResult discoveryResult;
    private final DependencyGraph dependencyGraph;
    private final Instant analyzedAt;

    public AnalysisResult(String migrationId, DiscoveryResult discoveryResult, DependencyGraph dependencyGraph) {
        this.migrationId = migrationId;
        this.discoveryResult = discoveryResult;
        this.dependencyGraph = dependencyGraph;
        this.analyzedAt = Instant.now();
    }

    public String getMigrationId() { return migrationId; }
    public DiscoveryResult getDiscoveryResult() { return discoveryResult; }
    public DependencyGraph getDependencyGraph() { return dependencyGraph; }
    public Instant getAnalyzedAt() { return analyzedAt; }
}
