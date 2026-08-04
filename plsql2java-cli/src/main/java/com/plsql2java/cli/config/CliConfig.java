package com.plsql2java.cli.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CliConfig {

    private Path configFile;
    private String jdbcUrl;
    private String jdbcUser;
    // char[] to limit credential exposure window; never stored as String
    private char[] jdbcPassword;
    private List<Path> ddlFiles = new ArrayList<>();
    private Path outputDir;
    private String targetPackage;
    private double confidenceThreshold = 0.7;
    private boolean verbose = false;
    private List<String> objectTypes = new ArrayList<>();

    public Path getConfigFile() { return configFile; }
    public void setConfigFile(Path configFile) { this.configFile = configFile; }

    public String getJdbcUrl() { return jdbcUrl; }
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }

    public String getJdbcUser() { return jdbcUser; }
    public void setJdbcUser(String jdbcUser) { this.jdbcUser = jdbcUser; }

    public char[] getJdbcPassword() { return jdbcPassword; }
    public void setJdbcPassword(char[] jdbcPassword) { this.jdbcPassword = jdbcPassword; }

    public List<Path> getDdlFiles() { return ddlFiles; }
    public void setDdlFiles(List<Path> ddlFiles) { this.ddlFiles = ddlFiles != null ? ddlFiles : new ArrayList<>(); }

    public Path getOutputDir() { return outputDir; }
    public void setOutputDir(Path outputDir) { this.outputDir = outputDir; }

    public String getTargetPackage() { return targetPackage; }
    public void setTargetPackage(String targetPackage) { this.targetPackage = targetPackage; }

    public double getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(double confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }

    public boolean isVerbose() { return verbose; }
    public void setVerbose(boolean verbose) { this.verbose = verbose; }

    public List<String> getObjectTypes() { return objectTypes; }
    public void setObjectTypes(List<String> objectTypes) { this.objectTypes = objectTypes != null ? objectTypes : new ArrayList<>(); }
}
