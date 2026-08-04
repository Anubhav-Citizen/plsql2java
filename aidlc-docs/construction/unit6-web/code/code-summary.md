# Code Summary — Unit 6: Web Application Delivery

## Module: plsql2java-web

**Package Root**: `com.plsql2java.web`  
**Stories Implemented**: 8.1 (DDL Upload), 8.2 (JDBC Config), 8.3 (Analysis View), 8.4 (Generate Trigger), 8.5 (Project Download), 8.6 (Report View)

---

## Application Code

### Entry Point
- `WebApplication.java` — `@SpringBootApplication` + `@EnableAsync` + `@EnableScheduling`; `ThreadPoolTaskExecutor` bean (core=2, max=5)

### Controllers (`controller/`)
- `MigrationController.java` — 10 REST endpoints under `/api/migrations/`; Stories 8.1–8.6; `@Valid` input validation
- `MigrationViewController.java` — Thymeleaf pages: `/`, `/progress/{jobId}`, `/report/{jobId}`
- `GlobalExceptionHandler.java` — `@RestControllerAdvice`; all exception types → generic JSON (SECURITY-09/15)

### Security (`security/`)
- `SecurityConfig.java` — deny-by-default; public paths; HTTP security headers (SECURITY-04/08); CORS explicit origins; form login; session management

### Services (`service/`)
- `MigrationJobService.java` — `@Async` `runAnalyze()`/`runGenerate()`; credential clearing; SSE registration
- `MigrationJobRegistry.java` — `ConcurrentHashMap`; `@Scheduled` 1-hour TTL cleanup; `JobNotFoundException`
- `FileUploadService.java` — sandbox, filename sanitization, path traversal prevention (BR-WEB-02/03, SECURITY-05)
- `CredentialStore.java` — `char[]` password storage; `clear()` zeroes array (SECURITY-12)

### Progress (`progress/`)
- `SseEmitterRegistry.java` — `ConcurrentHashMap<String, SseEmitter>`; 5-min timeout; fan-out emit; lifecycle management
- `WebProgressListener.java` — `MigrationProgressListener` impl; delegates to `SseEmitterRegistry`

### Packaging (`packaging/`)
- `ZipPackager.java` — streaming `ZipOutputStream`; excludes hidden files; `streamAndDelete()` cleans up temp ZIP

### Domain Models (`model/`)
- `MigrationJobState.java` — volatile fields for thread-safe status updates
- `MigrationJobResponse.java` — API response DTO
- `UploadedFile.java` — upload result value object
- `JdbcConfigRequest.java` — Bean Validation annotations
- `GenerateRequest.java` — Bean Validation annotations

### Resources
- `application.yml` — multipart limits, security user, output dir, Springdoc config
- Templates: `layout.html`, `index.html`, `progress.html`, `report.html`, `login.html` (all with `data-testid`)
- Static: `css/app.css`, `js/app.js` (SSE + form submission logic)

### Deployment
- `Dockerfile` — pinned `eclipse-temurin:17-jre-alpine`; non-root user 1001 (SECURITY-10)
- `docker-compose.yml` — `ADMIN_PASSWORD` required; output volume mount

---

## Tests

- `FileUploadServiceTest.java` — valid upload, non-SQL rejection, empty file, path traversal sanitization, delete
- `ZipPackagerTest.java` — ZIP creation, hidden file exclusion, stream-and-delete
- `MigrationJobRegistryTest.java` — register/get, unknown job exception, status update
- `MigrationControllerTest.java` — MockMvc: upload, analyze, generate, status 404, security headers, unauthenticated 401
- `WebIntegrationTest.java` — full Spring context: upload+analyze flow, home page auth redirect, JDBC config, invalid package 400

---

## Security Compliance

| Rule | Status | Notes |
|---|---|---|
| SECURITY-03 | Compliant | Passwords in `char[]`; cleared after use; never logged |
| SECURITY-04 | Compliant | CSP, HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy in SecurityConfig |
| SECURITY-05 | Compliant | File upload validation; filename sanitization; path traversal prevention |
| SECURITY-08 | Compliant | Deny-by-default; explicit public paths; no wildcard CORS |
| SECURITY-09 | Compliant | GlobalExceptionHandler returns generic messages; no stack traces |
| SECURITY-10 | Compliant | All deps pinned; Dockerfile uses pinned base image digest |
| SECURITY-11 | Compliant | Security logic isolated in SecurityConfig |
| SECURITY-12 | Compliant | CredentialStore with char[] + clear(); never echoed in responses |
| SECURITY-13 | Compliant | Jackson FAIL_ON_UNKNOWN_PROPERTIES; no unsafe deserialization |
| SECURITY-15 | Compliant | GlobalExceptionHandler catch-all; fail-closed on errors |
| SECURITY-01/02/06/07/14 | N/A | No persistent data store, no load balancer, no IAM, no monitoring infra |
