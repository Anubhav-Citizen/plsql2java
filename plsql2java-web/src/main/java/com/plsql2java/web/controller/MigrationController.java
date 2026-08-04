package com.plsql2java.web.controller;

import com.plsql2java.orchestration.model.MigrationJobStatus;
import com.plsql2java.web.model.*;
import com.plsql2java.web.packaging.ZipPackager;
import com.plsql2java.web.service.CredentialStore;
import com.plsql2java.web.service.FileUploadService;
import com.plsql2java.web.service.MigrationJobRegistry;
import com.plsql2java.web.service.MigrationJobService;
import com.plsql2java.web.progress.SseEmitterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/migrations")
@Tag(name = "Migrations", description = "PL/SQL to Java migration API")
public class MigrationController {

    private final FileUploadService uploadService;
    private final MigrationJobService jobService;
    private final MigrationJobRegistry jobRegistry;
    private final SseEmitterRegistry sseRegistry;
    private final ZipPackager zipPackager;
    private final CredentialStore credentialStore;

    public MigrationController(FileUploadService uploadService,
                                MigrationJobService jobService,
                                MigrationJobRegistry jobRegistry,
                                SseEmitterRegistry sseRegistry,
                                ZipPackager zipPackager,
                                CredentialStore credentialStore) {
        this.uploadService = uploadService;
        this.jobService = jobService;
        this.jobRegistry = jobRegistry;
        this.sseRegistry = sseRegistry;
        this.zipPackager = zipPackager;
        this.credentialStore = credentialStore;
    }

    /** Story 8.1 — DDL file upload */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a DDL SQL file for offline migration")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        UploadedFile uploaded = uploadService.store(file);
        return ResponseEntity.ok(Map.of(
                "uploadId", uploaded.getUploadId(),
                "filename", uploaded.getSanitizedFilename(),
                "sizeBytes", uploaded.getSizeBytes()
        ));
    }

    /** Story 8.2 — JDBC connection configuration */
    @PostMapping("/jdbc-config")
    @Operation(summary = "Submit JDBC connection configuration")
    public ResponseEntity<Map<String, String>> jdbcConfig(@Valid @RequestBody JdbcConfigRequest request) {
        String configId = UUID.randomUUID().toString();
        // Store password as char[] (SECURITY-12); never echo back
        credentialStore.store(configId, request.getPassword().toCharArray());
        // Clear String reference best-effort
        return ResponseEntity.ok(Map.of("configId", configId));
    }

    /** Story 8.3 — Trigger analysis */
    @PostMapping("/analyze")
    @Operation(summary = "Start discovery and dependency analysis")
    public ResponseEntity<Map<String, String>> analyze(@RequestBody Map<String, String> body) {
        String uploadId = body.get("uploadId");
        String configId = body.get("configId");
        if (uploadId == null && configId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "uploadId or configId is required"));
        }
        String jobId = jobService.submitAnalyze(uploadId, configId);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    /** Story 8.4 — Trigger full generation */
    @PostMapping("/generate")
    @Operation(summary = "Start full migration pipeline")
    public ResponseEntity<Map<String, String>> generate(@Valid @RequestBody GenerateRequest request) {
        if (request.getUploadId() == null && request.getConfigId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "uploadId or configId is required"));
        }
        String jobId = jobService.submitGenerate(request);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    /** Job status polling */
    @GetMapping("/{jobId}/status")
    @Operation(summary = "Get job status")
    public ResponseEntity<MigrationJobResponse> status(@PathVariable String jobId) {
        MigrationJobState state = jobRegistry.getOrThrow(jobId);
        return ResponseEntity.ok(new MigrationJobResponse(
                jobId, state.getStatus(), state.getLatestProgress(), state.getErrorMessage()));
    }

    /** SSE progress stream */
    @GetMapping(value = "/{jobId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to SSE progress events for a job")
    public SseEmitter events(@PathVariable String jobId) {
        jobRegistry.getOrThrow(jobId); // validate job exists
        return sseRegistry.register(jobId);
    }

    /** Story 8.3 — Analysis results */
    @GetMapping("/{jobId}/analysis")
    @Operation(summary = "Get analysis results for a completed analyze job")
    public ResponseEntity<?> analysisResult(@PathVariable String jobId) {
        MigrationJobState state = jobRegistry.getOrThrow(jobId);
        if (state.getAnalysisResult() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Analysis not yet complete"));
        }
        return ResponseEntity.ok(state.getAnalysisResult());
    }

    /** Story 8.5 — Download generated project as ZIP */
    @GetMapping("/{jobId}/download")
    @Operation(summary = "Download the generated project as a ZIP archive")
    public ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody> download(
            @PathVariable String jobId) {
        MigrationJobState state = jobRegistry.getOrThrow(jobId);
        if (state.getStatus() != MigrationJobStatus.COMPLETED && state.getStatus() != MigrationJobStatus.PARTIAL) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Path projectDir = state.getOutputDir().resolve("generated");
        org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody body = out -> {
            Path zipFile = zipPackager.packageProject(projectDir);
            zipPackager.streamAndDelete(zipFile, out);
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"generated-project.zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(body);
    }

    /** Story 8.6 — Report view */
    @GetMapping("/{jobId}/report")
    @Operation(summary = "Get migration report content")
    public ResponseEntity<?> report(@PathVariable String jobId) {
        MigrationJobState state = jobRegistry.getOrThrow(jobId);
        if (state.getMigrationResult() == null || state.getMigrationResult().getMigrationReport() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Report not yet available"));
        }
        var report = state.getMigrationResult().getMigrationReport();
        return ResponseEntity.ok(Map.of(
                "migrationId", report.getMigrationId(),
                "html", report.getHtmlContent() != null ? report.getHtmlContent() : "",
                "markdown", report.getMarkdownContent() != null ? report.getMarkdownContent() : ""
        ));
    }

    /** Story 8.6 — Report download */
    @GetMapping("/{jobId}/report/download")
    @Operation(summary = "Download the migration report")
    public ResponseEntity<String> reportDownload(@PathVariable String jobId,
                                                  @RequestParam(defaultValue = "html") String format) {
        MigrationJobState state = jobRegistry.getOrThrow(jobId);
        if (state.getMigrationResult() == null || state.getMigrationResult().getMigrationReport() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        var report = state.getMigrationResult().getMigrationReport();
        if ("md".equals(format)) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"migration-report.md\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(report.getMarkdownContent());
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"migration-report.html\"")
                .contentType(MediaType.TEXT_HTML)
                .body(report.getHtmlContent());
    }
}
