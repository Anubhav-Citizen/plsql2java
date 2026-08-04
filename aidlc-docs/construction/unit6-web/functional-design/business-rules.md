# Business Rules — Unit 6: Web Application Delivery

## BR-WEB-01: File Upload Validation
Uploaded files MUST be `.sql` extension, non-empty, and ≤ 50MB. Files failing any check are rejected with HTTP 400/413 before any processing.

## BR-WEB-02: Filename Sanitization
Uploaded filenames MUST be sanitized: strip all path separators (`/`, `\`), reject `..` sequences, allow only `[a-zA-Z0-9._-]`. Sanitized name used for storage; original name stored for display only. (SECURITY-05)

## BR-WEB-03: File Storage Sandboxing
Uploaded files MUST be stored in a sandboxed temp directory (`${java.io.tmpdir}/plsql2java/{uploadId}/`). The resolved path MUST be verified to be within the sandbox before any file operation (path traversal prevention). (SECURITY-05)

## BR-WEB-04: JDBC Password Handling
JDBC passwords submitted via API MUST NOT be echoed in any response body, log output, or SSE event. Passwords are stored only in server-side session memory and cleared after use. (SECURITY-03, SECURITY-12)

## BR-WEB-05: Target Package Validation
`targetPackage` MUST match `^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)*$`. Invalid values return HTTP 400.

## BR-WEB-06: Confidence Threshold Range
`confidenceThreshold` MUST be in [0.0, 1.0]. Values outside this range return HTTP 400.

## BR-WEB-07: Job Ownership
Download and report endpoints MUST verify the `jobId` exists. No cross-job data access. (SECURITY-08)

## BR-WEB-08: Download Only When Complete
`GET /api/migrations/{jobId}/download` MUST return HTTP 409 if the job status is not COMPLETED or PARTIAL. (SECURITY-15 fail-closed)

## BR-WEB-09: Generic Error Responses
Production error responses MUST NOT expose stack traces, internal paths, class names, or framework versions. All 5xx responses return `{ "error": "An internal error occurred" }`. (SECURITY-09)

## BR-WEB-10: HTTP Security Headers
All responses MUST include: `Content-Security-Policy`, `Strict-Transport-Security`, `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`. (SECURITY-04)

## BR-WEB-11: Authentication Required
All API endpoints and UI pages MUST require authentication except `GET /`, `GET /login`, and static assets. (SECURITY-08)

## BR-WEB-12: Request Body Size Limit
Maximum request body size: 50MB (enforced at Spring multipart and embedded Tomcat level). (SECURITY-05)

## BR-WEB-13: CORS Policy
CORS MUST NOT use `Access-Control-Allow-Origin: *` on authenticated endpoints. Allowed origins configured explicitly. (SECURITY-08)

## BR-WEB-14: Temp File Cleanup
Uploaded temp files and generated ZIP files MUST be deleted after use or after job expiry (1 hour). (SECURITY-09)

## BR-WEB-15: Async Job Execution
Migration jobs MUST run asynchronously. The HTTP response for `analyze` and `generate` MUST return 202 Accepted immediately, never blocking the request thread for the full pipeline duration.
