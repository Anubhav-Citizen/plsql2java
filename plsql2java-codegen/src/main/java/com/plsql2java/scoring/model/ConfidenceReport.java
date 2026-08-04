package com.plsql2java.scoring.model;

import java.time.Instant;
import java.util.List;

public class ConfidenceReport {

    private final String migrationId;
    private final int threshold;
    private final List<ObjectConfidenceScore> objectScores;
    private final int overallScore;
    private final int flaggedObjectCount;
    private final int flaggedMethodCount;
    private final Instant scoredAt;

    public ConfidenceReport(String migrationId, int threshold, List<ObjectConfidenceScore> objectScores,
                             int overallScore) {
        this.migrationId = migrationId;
        this.threshold = threshold;
        this.objectScores = objectScores;
        this.overallScore = overallScore;
        this.scoredAt = Instant.now();
        this.flaggedObjectCount = (int) objectScores.stream().filter(ObjectConfidenceScore::isBelowThreshold).count();
        this.flaggedMethodCount = (int) objectScores.stream()
                .flatMap(o -> o.getMethodScores().stream())
                .filter(MethodConfidenceScore::isBelowThreshold).count();
    }

    public String getMigrationId() { return migrationId; }
    public int getThreshold() { return threshold; }
    public List<ObjectConfidenceScore> getObjectScores() { return objectScores; }
    public int getOverallScore() { return overallScore; }
    public int getFlaggedObjectCount() { return flaggedObjectCount; }
    public int getFlaggedMethodCount() { return flaggedMethodCount; }
    public Instant getScoredAt() { return scoredAt; }
}
