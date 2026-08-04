package com.plsql2java.codegen.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GeneratedProject {

    private final String migrationId;
    private final String projectName;
    private final List<JavaSourceFile> files = new ArrayList<>();
    private final List<String> skippedObjects = new ArrayList<>();
    private final Instant generatedAt;

    public GeneratedProject(String migrationId, String projectName) {
        this.migrationId = migrationId;
        this.projectName = projectName;
        this.generatedAt = Instant.now();
    }

    public void addFile(JavaSourceFile file) { files.add(file); }
    public void addSkippedObject(String objectName) { skippedObjects.add(objectName); }

    public List<JavaSourceFile> getFilesByType(ArtifactType type) {
        return files.stream().filter(f -> f.getArtifactType() == type).collect(Collectors.toList());
    }

    public String getMigrationId() { return migrationId; }
    public String getProjectName() { return projectName; }
    public List<JavaSourceFile> getFiles() { return files; }
    public List<String> getSkippedObjects() { return skippedObjects; }
    public Instant getGeneratedAt() { return generatedAt; }
}
