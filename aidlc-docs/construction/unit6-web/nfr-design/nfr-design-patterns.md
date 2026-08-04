# NFR Design Patterns — Unit 6: Web Application Delivery

## Pattern 1: Deny-by-Default Security (SECURITY-08)

`SecurityConfig` configures Spring Security with `authorizeHttpRequests` defaulting to `authenticated()`. Only explicitly listed public paths (`/`, `/login`, `/css/**`, `/js/**`) are permitted without authentication. All other paths require authentication.

## Pattern 2: HTTP Security Headers Filter (SECURITY-04)

`SecurityConfig` adds all required headers via `headers()` DSL:
- `ContentSecurityPolicy`: `default-src 'self'`
- `StrictTransportSecurity`: `max-age=31536000; includeSubDomains`
- `X-Content-Type-Options`: `nosniff`
- `X-Frame-Options`: `DENY`
- `Referrer-Policy`: `strict-origin-when-cross-origin`

Applied to every response via Spring Security's `HeaderWriterFilter`.

## Pattern 3: Global Exception Handler (SECURITY-09 + SECURITY-15)

`@ControllerAdvice` class `GlobalExceptionHandler` catches:
- `ConstraintViolationException` → 400 with field-level message
- `MaxUploadSizeExceededException` → 413 with size limit message
- `JobNotFoundException` → 404
- `IllegalStateException` (job not complete) → 409
- `Exception` (catch-all) → 500 with generic `"An internal error occurred"` — no stack trace

## Pattern 4: Async Job Execution (BR-WEB-15)

`@Async` on `MigrationJobService.runAnalyze()` and `runGenerate()` methods. Spring `ThreadPoolTaskExecutor` configured with core=2, max=5, queue=10. Jobs tracked in `MigrationJobRegistry` (ConcurrentHashMap). Returns `jobId` immediately (202 Accepted).

## Pattern 5: SSE Fan-Out (Observer)

`SseEmitterRegistry` holds `ConcurrentHashMap<String, SseEmitter>` keyed by `jobId`. `WebProgressListener` calls `registry.emit(jobId, event)`. On client disconnect, emitter is removed. On job complete/error, emitter is completed and removed. Timeout: 5 minutes.

## Pattern 6: File Upload Sandboxing (SECURITY-05)

`FileUploadService.store()`:
1. Sanitize filename (strip path chars, reject `..`)
2. Resolve path: `sandboxRoot.resolve(uploadId).resolve(sanitizedName)`
3. Verify resolved path starts with `sandboxRoot` (path traversal check)
4. Write file only if check passes; throw `SecurityException` otherwise

## Pattern 7: Credential Isolation (SECURITY-12)

JDBC passwords submitted via API are stored in a `CredentialStore` (ConcurrentHashMap keyed by `configId`, values are `char[]`). Passwords are cleared from the store after `MigrationConfig` is built. Never serialized to JSON responses.

## Pattern 8: ZIP Streaming (Performance)

`ZipPackager.packageProject()` uses `ZipOutputStream` wrapped around a `BufferedOutputStream`. Files are streamed entry-by-entry — no full in-memory buffering. The ZIP is written to a temp file, then streamed to the HTTP response via `StreamingResponseBody`.
