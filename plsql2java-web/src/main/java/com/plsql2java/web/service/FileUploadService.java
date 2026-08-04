package com.plsql2java.web.service;

import com.plsql2java.web.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);
    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final long MAX_SIZE_BYTES = 50L * 1024 * 1024; // 50MB

    private final Path sandboxRoot;

    public FileUploadService(@Value("${plsql2java.upload.sandbox-dir:${java.io.tmpdir}/plsql2java}") String sandboxDir) throws IOException {
        this.sandboxRoot = Path.of(sandboxDir).toAbsolutePath().normalize();
        Files.createDirectories(sandboxRoot);
    }

    /**
     * Stores an uploaded DDL file in a sandboxed temp directory.
     * Validates extension, size, and filename. Prevents path traversal. (BR-WEB-01/02/03, SECURITY-05)
     */
    public UploadedFile store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new FileTooLargeException("File exceeds 50MB limit");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.sql";
        String sanitized = sanitizeFilename(originalFilename);

        if (!sanitized.toLowerCase().endsWith(".sql")) {
            throw new IllegalArgumentException("Only .sql files are accepted");
        }

        String uploadId = UUID.randomUUID().toString();
        Path uploadDir = sandboxRoot.resolve(uploadId).normalize();

        // BR-WEB-03: verify resolved path is within sandbox
        if (!uploadDir.startsWith(sandboxRoot)) {
            throw new SecurityException("Path traversal attempt detected");
        }

        Files.createDirectories(uploadDir);
        Path targetPath = uploadDir.resolve(sanitized).normalize();

        if (!targetPath.startsWith(uploadDir)) {
            throw new SecurityException("Path traversal attempt detected in filename");
        }

        Files.copy(file.getInputStream(), targetPath);
        log.debug("Stored upload {} ({} bytes) at {}", uploadId, file.getSize(), targetPath);

        return new UploadedFile(uploadId, originalFilename, sanitized, targetPath, file.getSize());
    }

    public Path resolve(String uploadId) {
        Path uploadDir = sandboxRoot.resolve(uploadId).normalize();
        if (!uploadDir.startsWith(sandboxRoot)) {
            throw new SecurityException("Invalid uploadId");
        }
        return uploadDir;
    }

    public void delete(String uploadId) {
        try {
            Path uploadDir = resolve(uploadId);
            if (Files.isDirectory(uploadDir)) {
                try (var stream = Files.walk(uploadDir)) {
                    stream.sorted(java.util.Comparator.reverseOrder())
                          .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
                }
            }
        } catch (IOException e) {
            log.warn("Failed to delete upload {}: {}", uploadId, e.getMessage());
        }
    }

    /** BR-WEB-02: strip path separators, reject .., allow only safe characters. */
    private String sanitizeFilename(String filename) {
        // Strip any path component
        String name = Path.of(filename).getFileName().toString();
        // Replace unsafe characters
        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!SAFE_FILENAME.matcher(name).matches() || name.contains("..")) {
            throw new IllegalArgumentException("Invalid filename: " + filename);
        }
        return name;
    }

    public static class FileTooLargeException extends RuntimeException {
        public FileTooLargeException(String message) { super(message); }
    }
}
