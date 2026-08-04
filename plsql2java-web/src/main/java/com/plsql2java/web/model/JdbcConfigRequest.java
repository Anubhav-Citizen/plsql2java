package com.plsql2java.web.model;

import jakarta.validation.constraints.*;

public class JdbcConfigRequest {

    @NotBlank(message = "jdbcUrl is required")
    private String jdbcUrl;

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

    @Pattern(regexp = "^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$",
             message = "targetPackage must be a valid Java package identifier")
    private String targetPackage = "com.example.migrated";

    @DecimalMin(value = "0.0", message = "confidenceThreshold must be >= 0.0")
    @DecimalMax(value = "1.0", message = "confidenceThreshold must be <= 1.0")
    private double confidenceThreshold = 0.7;

    public String getJdbcUrl() { return jdbcUrl; }
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTargetPackage() { return targetPackage; }
    public void setTargetPackage(String targetPackage) { this.targetPackage = targetPackage; }
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(double confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }
}
