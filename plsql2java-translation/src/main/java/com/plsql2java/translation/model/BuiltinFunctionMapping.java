package com.plsql2java.translation.model;

public class BuiltinFunctionMapping {

    private String oracleFunction;
    private String javaEquivalent;
    private String requiresImport;
    private String notes;

    public BuiltinFunctionMapping() {}

    public String getOracleFunction() { return oracleFunction; }
    public void setOracleFunction(String v) { this.oracleFunction = v; }
    public String getJavaEquivalent() { return javaEquivalent; }
    public void setJavaEquivalent(String v) { this.javaEquivalent = v; }
    public String getRequiresImport() { return requiresImport; }
    public void setRequiresImport(String v) { this.requiresImport = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
}
