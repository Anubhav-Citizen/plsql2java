# NFR Requirements — Unit 6: Web Application Delivery

## Performance
- API response for `analyze`/`generate` submission: < 200ms (202 Accepted, async)
- SSE event delivery latency: < 500ms from orchestrator emit to browser
- ZIP packaging: streaming (no full in-memory buffering for large projects)
- Concurrent jobs: support at least 3 simultaneous migration jobs (in-memory job registry)

## Security
- SECURITY-03: No passwords, tokens, or PII in log output
- SECURITY-04: HTTP security headers on all responses (CSP, HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy)
- SECURITY-05: File upload validation (size, extension, filename sanitization, path traversal prevention)
- SECURITY-08: Spring Security authentication on all non-public endpoints; deny-by-default; no wildcard CORS on authenticated endpoints
- SECURITY-09: Generic error messages in production; no stack traces in HTTP responses
- SECURITY-10: All dependencies pinned; no `latest` tags in Dockerfile
- SECURITY-11: Security logic isolated in `SecurityConfig`; rate limiting on upload endpoint
- SECURITY-12: Password never echoed in responses; session-based credential storage
- SECURITY-13: No unsafe deserialization; Jackson configured with `FAIL_ON_UNKNOWN_PROPERTIES`
- SECURITY-15: Global `@ControllerAdvice` exception handler; fail-closed on errors

## Reliability
- Async job execution via `@Async` (Spring TaskExecutor) — no blocking request threads
- SSE emitters cleaned up on client disconnect and job completion
- Temp files cleaned up after job expiry (1 hour scheduled task)
- Job registry survives individual request failures (in-memory, not request-scoped)

## Usability
- Progress bar updates in real-time via SSE (no polling required)
- Download button appears automatically when job completes
- Error messages are human-readable and actionable
- All interactive elements have `data-testid` attributes

## Maintainability
- Controller thin: delegates all business logic to orchestrator
- `SecurityConfig` is the single place for all security rules
- `SseEmitterRegistry` manages all active SSE connections
- `ZipPackager` is independently testable (no Spring dependency)

## Packaging
- Self-contained Docker container via `spring-boot-maven-plugin` + Dockerfile
- Runs on port 8080 (configurable via `SERVER_PORT` env var)
- No external database or message broker required
