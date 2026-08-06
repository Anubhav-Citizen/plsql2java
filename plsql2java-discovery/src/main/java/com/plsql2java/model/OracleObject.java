package com.plsql2java.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class OracleObject {

    private String name;
    private OracleObjectType type;
    private String schema;
    private String sourceSpec;
    private String sourceBody;
    private boolean hasCompilationErrors;
    private int lineCount;

    public OracleObject() {}

    public OracleObject(String name, OracleObjectType type, String schema, String sourceSpec) {
        this.name = name;
        this.type = type;
        this.schema = schema;
        this.sourceSpec = sourceSpec;
        this.lineCount = sourceSpec != null ? sourceSpec.split("\n").length : 0;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public OracleObjectType getType() { return type; }
    public void setType(OracleObjectType type) { this.type = type; }

    public String getSchema() { return schema; }
    public void setSchema(String schema) { this.schema = schema; }

    public String getSourceSpec() { return sourceSpec; }
    public void setSourceSpec(String sourceSpec) {
        this.sourceSpec = sourceSpec;
        this.lineCount = sourceSpec != null ? sourceSpec.split("\n").length : 0;
    }

    public String getSourceBody() { return sourceBody; }
    public void setSourceBody(String sourceBody) { this.sourceBody = sourceBody; }

    public boolean isHasCompilationErrors() { return hasCompilationErrors; }
    public void setHasCompilationErrors(boolean hasCompilationErrors) { this.hasCompilationErrors = hasCompilationErrors; }

    public int getLineCount() { return lineCount; }
    public void setLineCount(int lineCount) { this.lineCount = lineCount; }

    /** Returns the full source (spec + body if present). */
    @JsonIgnore
    public String getFullSource() {
        if (sourceBody != null && !sourceBody.isBlank()) {
            return sourceSpec + "\n" + sourceBody;
        }
        return sourceSpec != null ? sourceSpec : "";
    }

    @Override
    public String toString() {
        return schema + "." + name + " [" + type + "]";
    }
}
