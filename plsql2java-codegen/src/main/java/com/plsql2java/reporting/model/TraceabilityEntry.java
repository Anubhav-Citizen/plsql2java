package com.plsql2java.reporting.model;

import com.plsql2java.model.OracleObjectType;

import java.util.List;

public class TraceabilityEntry {

    private final String plsqlObjectName;
    private final OracleObjectType plsqlObjectType;
    private final String javaClassName;
    private final List<String> javaMethodNames;
    private final TraceabilityStatus status;
    private final int confidenceScore;

    public TraceabilityEntry(String plsqlObjectName, OracleObjectType plsqlObjectType,
                              String javaClassName, List<String> javaMethodNames,
                              TraceabilityStatus status, int confidenceScore) {
        this.plsqlObjectName = plsqlObjectName;
        this.plsqlObjectType = plsqlObjectType;
        this.javaClassName = javaClassName;
        this.javaMethodNames = javaMethodNames;
        this.status = status;
        this.confidenceScore = confidenceScore;
    }

    public String getPlsqlObjectName() { return plsqlObjectName; }
    public OracleObjectType getPlsqlObjectType() { return plsqlObjectType; }
    public String getJavaClassName() { return javaClassName; }
    public List<String> getJavaMethodNames() { return javaMethodNames; }
    public TraceabilityStatus getStatus() { return status; }
    public int getConfidenceScore() { return confidenceScore; }
}
