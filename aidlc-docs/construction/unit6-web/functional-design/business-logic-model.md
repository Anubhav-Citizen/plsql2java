# Business Logic Model — Unit 6: Web Application Delivery

## Overview
Unit 6 is the web delivery layer. It exposes a REST API backed by Spring Boot, streams progress via SSE, handles multipart DDL uploads, packages generated output as a downloadable ZIP, and serves a Thymeleaf-based frontend UI.

---

## Story 8.1 — DDL File Upload

**Endpoint**: `POST /api/migrations/upload`  
**Content-Type**: `multipart/form-data`

**Algorithm**:
1. Validate file: non-empty, `.sql` extension, size ≤ 50MB (configurable)
2. Sanitize filename: strip path separators, reject `..` traversal (SECURITY-05)
3. Write to sandboxed temp directory (`${java.io.tmpdir}/plsql2java/{uuid}/`)
4. Return `{ "uploadId": "<uuid>", "filename": "<sanitized>", "sizeBytes": N }`

**Validation rules**:
- Max file size: 50MB (Spring `spring.servlet.multipart.max-file-size`)
- Allowed extensions: `.sql` only
- Filename: alphanumeric, hyphens, underscores, dots only (after sanitization)

---

## Story 8.2 — JDBC Connection Configuration

**Endpoint**: `POST /api/migrations/jdbc-config`  
**Content-Type**: `application/json`

**Request body**:
```json
{
  "jdbcUrl": "jdbc:oracle:thin:@host:1521:XE",
  "username": "scott",
  "password": "...",
  "targetPackage": "com.example.migrated",
  "confidenceThreshold": 0.7
}
```

**Algorithm**:
1. Validate all fields (BR-WEB-01 through BR-WEB-04)
2. Store config in server-side session (keyed by `configId` UUID)
3. Return `{ "configId": "<uuid>" }`
4. Password is never echoed back in any response

---

## Story 8.3 — Discovery and Analysis Results View

**Endpoint**: `POST /api/migrations/analyze`  
**Request**: `{ "uploadId": "<uuid>" }` or `{ "configId": "<uuid>" }`

**Algorithm**:
1. Resolve MigrationConfig from uploadId (file mode) or configId (JDBC mode)
2. Assign `jobId` UUID
3. Register `WebProgressListener` for this jobId on `ProgressEventBus`
4. Submit `orchestrator.analyze(config)` asynchronously (`@Async`)
5. Return `{ "jobId": "<uuid>" }` immediately (202 Accepted)
6. Client polls `GET /api/migrations/{jobId}/status` or subscribes to SSE

**SSE stream**: `GET /api/migrations/{jobId}/events`
- Emits `MigrationProgress` events as JSON until COMPLETE or ERROR

**Results**: `GET /api/migrations/{jobId}/analysis`
- Returns `AnalysisResult` summary (object counts, dependency stats, migration order)

---

## Story 8.4 — Code Generation Trigger

**Endpoint**: `POST /api/migrations/generate`  
**Request**: `{ "uploadId": "<uuid>", "targetPackage": "...", "confidenceThreshold": 0.7 }` or `{ "configId": "<uuid>" }`

**Algorithm**:
1. Resolve MigrationConfig
2. Assign `jobId`, register `WebProgressListener`
3. Submit `orchestrator.generate(config)` asynchronously
4. Return `{ "jobId": "<uuid>" }` (202 Accepted)
5. Progress streamed via SSE

---

## Story 8.5 — Generated Project Download

**Endpoint**: `GET /api/migrations/{jobId}/download`

**Algorithm**:
1. Verify job exists and status is COMPLETED or PARTIAL
2. Locate generated project directory for jobId
3. Call `ZipPackager.packageProject(projectDir)` → `Path zipFile`
4. Stream ZIP as `application/zip` with `Content-Disposition: attachment; filename="generated-project.zip"`
5. Delete temp ZIP after streaming (cleanup)

**ZipPackager logic**:
- Recursively adds all files under `generated/` to ZIP
- Preserves relative directory structure
- Skips hidden files and `.DS_Store`

---

## Story 8.6 — Migration Report View and Download

**Endpoints**:
- `GET /api/migrations/{jobId}/report` — returns report metadata + HTML content
- `GET /api/migrations/{jobId}/report/download` — streams report as file download

**Algorithm**:
1. Verify job exists and has a completed report
2. Load `MigrationReport` from job result
3. For view: return `{ "html": "...", "markdown": "..." }`
4. For download: stream as `text/html` or `text/markdown` based on `?format=html|md`

---

## Frontend UI (Thymeleaf)

**Pages**:
- `GET /` — home page with upload form and JDBC config form
- `GET /analyze` — analysis results view (polls job status)
- `GET /generate` — generation progress view (SSE-connected)
- `GET /report/{jobId}` — embedded HTML report view

**UI flow**:
1. User uploads DDL file or enters JDBC config → triggers analyze or generate
2. Progress bar updates via SSE
3. On completion: download button appears for ZIP; report link appears
4. All forms use `data-testid` attributes for automation

---

## Job State Management (In-Memory)

Jobs are tracked in a `ConcurrentHashMap<String, MigrationJobState>` in `MigrationJobRegistry`:
- `jobId → { status, config, result, outputDir, startedAt }`
- No persistence (in-memory only for this delivery)
- Jobs expire after 1 hour (scheduled cleanup)

---

## Error Handling

| Error | HTTP Status | Response |
|---|---|---|
| File too large | 413 | `{ "error": "File exceeds 50MB limit" }` |
| Invalid file type | 400 | `{ "error": "Only .sql files are accepted" }` |
| Job not found | 404 | `{ "error": "Job not found" }` |
| Job not complete | 409 | `{ "error": "Job not yet complete" }` |
| Orchestrator failure | 500 | `{ "error": "Migration failed" }` (no internal details) |
| Validation failure | 400 | `{ "error": "<field>: <message>" }` |
