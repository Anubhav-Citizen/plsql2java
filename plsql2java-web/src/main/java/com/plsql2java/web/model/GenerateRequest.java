package com.plsql2java.web.model;

import jakarta.validation.constraints.*;

public class GenerateRequest {

    private String uploadId;
    private String configId;

    @Pattern(regexp = "^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$",
             message = "targetPackage must be a valid Java package identifier")
    private String targetPackage = "com.example.migrated";

    @DecimalMin(value = "0.0") @DecimalMax(value = "1.0")
    private double confidenceThreshold = 0.7;

    public String getUploadId() { return uploadId; }
    public void setUploadId(String uploadId) { this.uploadId = uploadId; }
    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }
    public String getTargetPackage() { return targetPackage; }
    public void setTargetPackage(String targetPackage) { this.targetPackage = targetPackage; }
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(double v) { this.confidenceThreshold = v; }
}
