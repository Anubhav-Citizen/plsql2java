package com.plsql2java.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DependencyGraph {

    private String migrationId;
    private List<DependencyEdge> edges = new ArrayList<>();
    private List<CircularDependency> circularDependencies = new ArrayList<>();
    private List<String> migrationOrder = new ArrayList<>();
    private List<String> leafObjects = new ArrayList<>();
    private Instant analyzedAt;

    public DependencyGraph() {}

    public DependencyGraph(String migrationId) {
        this.migrationId = migrationId;
        this.analyzedAt = Instant.now();
    }

    public String getMigrationId() { return migrationId; }
    public void setMigrationId(String migrationId) { this.migrationId = migrationId; }

    public List<DependencyEdge> getEdges() { return edges; }
    public void setEdges(List<DependencyEdge> edges) { this.edges = edges; }

    public List<CircularDependency> getCircularDependencies() { return circularDependencies; }
    public void setCircularDependencies(List<CircularDependency> circularDependencies) { this.circularDependencies = circularDependencies; }

    public List<String> getMigrationOrder() { return migrationOrder; }
    public void setMigrationOrder(List<String> migrationOrder) { this.migrationOrder = migrationOrder; }

    public List<String> getLeafObjects() { return leafObjects; }
    public void setLeafObjects(List<String> leafObjects) { this.leafObjects = leafObjects; }

    public Instant getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(Instant analyzedAt) { this.analyzedAt = analyzedAt; }

    public boolean hasCircularDependencies() { return !circularDependencies.isEmpty(); }
}
