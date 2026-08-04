package com.plsql2java.codegen.model;

public class JavaSourceFile {

    private final String relativePath;
    private final String content;
    private final String sourceObjectName;
    private final ArtifactType artifactType;

    public JavaSourceFile(String relativePath, String content, String sourceObjectName, ArtifactType artifactType) {
        this.relativePath = relativePath;
        this.content = content;
        this.sourceObjectName = sourceObjectName;
        this.artifactType = artifactType;
    }

    public String getRelativePath() { return relativePath; }
    public String getContent() { return content; }
    public String getSourceObjectName() { return sourceObjectName; }
    public ArtifactType getArtifactType() { return artifactType; }
}
