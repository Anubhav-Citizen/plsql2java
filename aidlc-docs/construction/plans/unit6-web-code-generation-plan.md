# Unit 6 — Web Application Delivery — Code Generation Plan

**Unit**: plsql2java-web  
**Package Root**: com.plsql2java.web  
**Stories**: 8.1 (DDL Upload), 8.2 (JDBC Config), 8.3 (Analysis View), 8.4 (Generate Trigger), 8.5 (Project Download), 8.6 (Report View)  
**Dependencies**: plsql2java-orchestrator (→ all upstream units)

---

## Unit Context

- **Delivery type**: Spring Boot Web + Thymeleaf + Spring Security + SSE
- **Key classes**: WebApplication, MigrationController, MigrationViewController, SecurityConfig, MigrationJobService, MigrationJobRegistry, FileUploadService, WebProgressListener, SseEmitterRegistry, ZipPackager, CredentialStore, GlobalExceptionHandler
- **Async**: `@Async` job execution via `ThreadPoolTaskExecutor`
- **Frontend**: Thymeleaf templates + Bootstrap 5 (local static assets) + SSE JS
- **Security**: SECURITY-03/04/05/08/09/10/11/12/13/15 all enforced
- **Packaging**: Fat JAR + Dockerfile + docker-compose.yml

---

## Steps

- [x] **Step 1**: Create `plsql2java-web/pom.xml`
  - Dependencies: plsql2java-orchestrator, spring-boot-starter-web, spring-boot-starter-thymeleaf, spring-boot-starter-security, springdoc-openapi-starter-webmvc-ui
  - spring-boot-maven-plugin repackage; mainClass = com.plsql2java.web.WebApplication
  - Stories: infrastructure for 8.1–8.6

- [x] **Step 2**: Create domain model — `MigrationJobState.java`, `MigrationJobRequest.java`, `MigrationJobResponse.java`, `UploadedFile.java`
  - Stories: 8.1–8.6

- [x] **Step 3**: Create `MigrationJobRegistry.java` + `CredentialStore.java`
  - ConcurrentHashMap-based; scheduled cleanup (1 hour TTL)
  - Stories: 8.2–8.6

- [x] **Step 4**: Create `FileUploadService.java`
  - Sandbox validation, filename sanitization, path traversal prevention (BR-WEB-02/03)
  - Stories: 8.1

- [x] **Step 5**: Create `SseEmitterRegistry.java` + `WebProgressListener.java`
  - SSE fan-out; emitter lifecycle management; JSON serialization of MigrationProgress
  - Stories: 8.3, 8.4

- [x] **Step 6**: Create `ZipPackager.java`
  - Streaming ZIP via ZipOutputStream; StreamingResponseBody; temp file cleanup
  - Story: 8.5

- [x] **Step 7**: Create `MigrationJobService.java`
  - @Async runAnalyze() + runGenerate(); builds MigrationConfig; registers WebProgressListener; updates registry
  - Stories: 8.3, 8.4

- [x] **Step 8**: Create `MigrationController.java` (REST API)
  - Endpoints: POST /upload, POST /jdbc-config, POST /analyze, POST /generate, GET /{jobId}/status, GET /{jobId}/events (SSE), GET /{jobId}/download, GET /{jobId}/analysis, GET /{jobId}/report, GET /{jobId}/report/download
  - @Valid input validation; thin delegation to services
  - Stories: 8.1–8.6

- [x] **Step 9**: Create `MigrationViewController.java` (Thymeleaf UI)
  - GET /, GET /progress/{jobId}, GET /report/{jobId}
  - Stories: 8.3, 8.4, 8.6

- [x] **Step 10**: Create `SecurityConfig.java`
  - Deny-by-default; public paths; HTTP security headers (SECURITY-04/08); CORS; form login; session management
  - Stories: all (cross-cutting)

- [x] **Step 11**: Create `GlobalExceptionHandler.java`
  - @ControllerAdvice; all exception types → generic JSON responses (SECURITY-09/15)
  - Stories: all (cross-cutting)

- [x] **Step 12**: Create `WebApplication.java` + `application.yml`
  - @SpringBootApplication + @EnableAsync; ThreadPoolTaskExecutor bean; multipart config; output dir config
  - Stories: all

- [x] **Step 13**: Create Thymeleaf templates
  - `layout.html`, `index.html`, `progress.html`, `report.html`, `login.html`
  - data-testid attributes on all interactive elements
  - Stories: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6

- [x] **Step 14**: Create static assets
  - `static/css/app.css` (minimal custom styles)
  - `static/js/progress.js` (SSE client logic)
  - Stories: 8.3, 8.4

- [x] **Step 15**: Create `Dockerfile` + `docker-compose.yml`
  - Pinned base image; non-root user; EXPOSE 8080
  - Story: all (deployment)

- [x] **Step 16**: Create unit tests — `FileUploadServiceTest.java`, `ZipPackagerTest.java`, `MigrationJobRegistryTest.java`, `SseEmitterRegistryTest.java`
  - Stories: 8.1, 8.5

- [x] **Step 17**: Create unit tests — `MigrationControllerTest.java` (MockMvc)
  - Upload, JDBC config, analyze, generate, download, report endpoints
  - Mock MigrationJobService; verify HTTP status codes, headers, response bodies
  - Stories: 8.1–8.6

- [x] **Step 18**: Create `WebIntegrationTest.java`
  - Spring context wired; mock orchestrator; full request/response cycle
  - Stories: 8.1–8.6

- [x] **Step 19**: Create `aidlc-docs/construction/unit6-web/code/code-summary.md`

---

## Story Traceability

| Story | Steps |
|---|---|
| 8.1 DDL File Upload | 1, 2, 3, 4, 8, 12, 13, 14, 16, 17, 18 |
| 8.2 JDBC Connection Config | 1, 2, 3, 8, 10, 12, 13, 17, 18 |
| 8.3 Analysis Results View | 1, 2, 3, 5, 7, 8, 9, 12, 13, 14, 17, 18 |
| 8.4 Code Generation Trigger | 1, 2, 3, 5, 7, 8, 9, 12, 13, 14, 17, 18 |
| 8.5 Generated Project Download | 1, 2, 6, 8, 12, 13, 16, 17, 18 |
| 8.6 Report View and Download | 1, 2, 8, 9, 12, 13, 17, 18 |

---

## Security Compliance Checklist

| Rule | Step(s) | Status |
|---|---|---|
| SECURITY-03 (no credentials in logs) | 3, 7, 8 | Enforced |
| SECURITY-04 (HTTP security headers) | 10 | Enforced |
| SECURITY-05 (input validation) | 4, 8, 12 | Enforced |
| SECURITY-08 (access control) | 10 | Enforced |
| SECURITY-09 (generic error messages) | 11 | Enforced |
| SECURITY-10 (pinned deps) | 1, 15 | Enforced |
| SECURITY-11 (secure design) | 10 | Enforced |
| SECURITY-12 (credential mgmt) | 3, 7 | Enforced |
| SECURITY-13 (integrity) | 8, 12 | Enforced |
| SECURITY-15 (exception handling) | 11 | Enforced |
| SECURITY-01 (encryption at rest) | N/A — no persistent data store | N/A |
| SECURITY-02 (access logging) | N/A — no load balancer/API gateway | N/A |
| SECURITY-06 (least privilege) | N/A — no IAM | N/A |
| SECURITY-07 (network config) | N/A — no cloud network config | N/A |
| SECURITY-14 (alerting) | N/A — no monitoring infra | N/A |
