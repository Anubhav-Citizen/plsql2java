package com.plsql2java.translation.model;

public class FlaggedConstruct {

    private final String objectName;
    private final ConstructType constructType;
    private final int lineNumber;
    private final String reason;
    private final String recommendation;

    public FlaggedConstruct(String objectName, ConstructType constructType, int lineNumber,
                            String reason, String recommendation) {
        this.objectName = objectName;
        this.constructType = constructType;
        this.lineNumber = lineNumber;
        this.reason = reason;
        this.recommendation = recommendation;
    }

    public String getObjectName() { return objectName; }
    public ConstructType getConstructType() { return constructType; }
    public int getLineNumber() { return lineNumber; }
    public String getReason() { return reason; }
    public String getRecommendation() { return recommendation; }
}
