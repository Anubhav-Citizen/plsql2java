package com.plsql2java.translation.model;

public class OracleExceptionMapping {

    private String oracleException;
    private String javaException;
    private String requiresImport;

    public OracleExceptionMapping() {}

    public String getOracleException() { return oracleException; }
    public void setOracleException(String v) { this.oracleException = v; }
    public String getJavaException() { return javaException; }
    public void setJavaException(String v) { this.javaException = v; }
    public String getRequiresImport() { return requiresImport; }
    public void setRequiresImport(String v) { this.requiresImport = v; }
}
