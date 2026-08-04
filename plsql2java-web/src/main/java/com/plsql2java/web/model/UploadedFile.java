package com.plsql2java.web.model;

import java.nio.file.Path;

public class UploadedFile {

    private final String uploadId;
    private final String originalFilename;
    private final String sanitizedFilename;
    private final Path tempPath;
    private final long sizeBytes;

    public UploadedFile(String uploadId, String originalFilename, String sanitizedFilename,
                        Path tempPath, long sizeBytes) {
        this.uploadId = uploadId;
        this.originalFilename = originalFilename;
        this.sanitizedFilename = sanitizedFilename;
        this.tempPath = tempPath;
        this.sizeBytes = sizeBytes;
    }

    public String getUploadId() { return uploadId; }
    public String getOriginalFilename() { return originalFilename; }
    public String getSanitizedFilename() { return sanitizedFilename; }
    public Path getTempPath() { return tempPath; }
    public long getSizeBytes() { return sizeBytes; }
}
