package com.plsql2java.scoring.model;

import java.util.List;

public class MethodConfidenceScore {

    private final String objectName;
    private final String methodName;
    private final int score;
    private final boolean belowThreshold;
    private final List<String> penaltyReasons;

    public MethodConfidenceScore(String objectName, String methodName, int score,
                                  boolean belowThreshold, List<String> penaltyReasons) {
        this.objectName = objectName;
        this.methodName = methodName;
        this.score = score;
        this.belowThreshold = belowThreshold;
        this.penaltyReasons = penaltyReasons;
    }

    public String getObjectName() { return objectName; }
    public String getMethodName() { return methodName; }
    public int getScore() { return score; }
    public boolean isBelowThreshold() { return belowThreshold; }
    public List<String> getPenaltyReasons() { return penaltyReasons; }
}
