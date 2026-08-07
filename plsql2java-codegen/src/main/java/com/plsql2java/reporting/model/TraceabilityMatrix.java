package com.plsql2java.reporting.model;

import java.util.List;

public class TraceabilityMatrix {

    private final List<TraceabilityEntry> entries;
    private final double coveragePct;

    public TraceabilityMatrix(List<TraceabilityEntry> entries) {
        this.entries = entries;
        long covered = entries.stream()
                .filter(e -> e.getStatus() == TraceabilityStatus.MIGRATED
                        || e.getStatus() == TraceabilityStatus.PARTIAL
                        || e.getStatus() == TraceabilityStatus.SKIPPED)
                .count();
        this.coveragePct = entries.isEmpty() ? 100.0 : (covered * 100.0 / entries.size());
    }

    public List<TraceabilityEntry> getEntries() { return entries; }
    public double getCoveragePct() { return coveragePct; }
}
