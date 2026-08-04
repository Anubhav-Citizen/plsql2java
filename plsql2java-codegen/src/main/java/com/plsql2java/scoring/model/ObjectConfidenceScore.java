package com.plsql2java.scoring.model;

import com.plsql2java.model.OracleObjectType;

import java.util.List;

public class ObjectConfidenceScore {

    private final String objectName;
    private final OracleObjectType objectType;
    private final int score;
    private final boolean belowThreshold;
    private final List<MethodConfidenceScore> methodScores;
    private final boolean hasCompilationErrors;

    public ObjectConfidenceScore(String objectName, OracleObjectType objectType, int score,
                                  boolean belowThreshold, List<MethodConfidenceScore> methodScores,
                                  boolean hasCompilationErrors) {
        this.objectName = objectName;
        this.objectType = objectType;
        this.score = score;
        this.belowThreshold = belowThreshold;
        this.methodScores = methodScores;
        this.hasCompilationErrors = hasCompilationErrors;
    }

    public String getObjectName() { return objectName; }
    public OracleObjectType getObjectType() { return objectType; }
    public int getScore() { return score; }
    public boolean isBelowThreshold() { return belowThreshold; }
    public List<MethodConfidenceScore> getMethodScores() { return methodScores; }
    public boolean isHasCompilationErrors() { return hasCompilationErrors; }
}
