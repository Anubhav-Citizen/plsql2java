# Domain Entities — Unit 6: Web Application Delivery

## Entities Owned by Unit 6

Unit 6 owns no new domain entities. It consumes entities from upstream units and introduces web-specific value objects.

## Web-Specific Value Objects

### UploadedFile
- `originalFilename: String` — sanitized original filename
- `tempPath: Path` — server-side temp path (sandboxed)
- `sizeBytes: long`
- `contentType: String`

### MigrationJobRequest
- `jobId: String` — UUID assigned at submission
- `mode: OperationMode` — ANALYZE | GENERATE | REPORT
- `config: MigrationConfig` — built from form/API input
- `submittedAt: Instant`

### MigrationJobResponse
- `jobId: String`
- `status: MigrationJobStatus`
- `progress: MigrationProgress` (latest)
- `resultPath: String` (nullable — set when complete)
- `errorMessage: String` (nullable)

### SseEvent
- `eventType: String` — "progress" | "complete" | "error"
- `data: String` — JSON-serialized MigrationProgress or result summary

### ZipDownloadRequest
- `jobId: String`
- `outputDir: Path`

## Consumed Upstream Entities
- `MigrationConfig` (plsql2java-discovery)
- `MigrationResult`, `AnalysisResult`, `MigrationProgress` (plsql2java-orchestrator)
- `MigrationReport` (plsql2java-codegen)

## Relationships
```
HTTP Request ──parsed-by──> MigrationController
MigrationController ──builds──> MigrationJobRequest
MigrationJobRequest ──submitted-to──> MigrationOrchestratorService
MigrationProgress ──routed-via──> WebProgressListener ──emitted-to──> SseEmitterRegistry
SseEmitterRegistry ──streams-to──> Browser (SSE)
GeneratedProject ──packaged-by──> ZipPackager ──downloaded-by──> Browser
```
