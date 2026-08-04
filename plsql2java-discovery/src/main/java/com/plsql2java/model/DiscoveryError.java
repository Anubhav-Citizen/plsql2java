package com.plsql2java.model;

public class DiscoveryError {

    private String source;
    private DiscoveryErrorType errorType;
    private String message;
    private Integer lineNumber;

    public DiscoveryError() {}

    public DiscoveryError(String source, DiscoveryErrorType errorType, String message) {
        this.source = source;
        this.errorType = errorType;
        this.message = message;
    }

    public DiscoveryError(String source, DiscoveryErrorType errorType, String message, Integer lineNumber) {
        this(source, errorType, message);
        this.lineNumber = lineNumber;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public DiscoveryErrorType getErrorType() { return errorType; }
    public void setErrorType(DiscoveryErrorType errorType) { this.errorType = errorType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
}
