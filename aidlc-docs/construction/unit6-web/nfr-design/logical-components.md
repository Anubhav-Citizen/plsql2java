# Logical Components — Unit 6: Web Application Delivery

## Component: MigrationController (REST API)
- `@RestController`, `@RequestMapping("/api/migrations")`
- Endpoints: upload, jdbc-config, analyze, generate, download, report, events (SSE), status
- Thin: delegates to `MigrationJobService`, `FileUploadService`, `ZipPackager`
- Input validation via `@Valid` + Bean Validation annotations

## Component: MigrationViewController (Thymeleaf UI)
- `@Controller`, `@RequestMapping("/")`
- Serves: `/` (home), `/progress/{jobId}`, `/report/{jobId}`
- Passes model attributes to Thymeleaf templates

## Component: MigrationJobService
- `@Service`, `@Async` methods for `runAnalyze()` and `runGenerate()`
- Builds `MigrationConfig` from request, registers `WebProgressListener`, calls orchestrator
- Updates `MigrationJobRegistry` with status and result
- Clears credentials from `CredentialStore` after config is built

## Component: MigrationJobRegistry
- `@Component`, `ConcurrentHashMap<String, MigrationJobState>`
- `register(jobId, state)`, `get(jobId)`, `updateStatus(jobId, status)`, `setResult(jobId, result)`
- `@Scheduled` cleanup: removes jobs older than 1 hour

## Component: FileUploadService
- `@Service`
- `store(MultipartFile, uploadId): UploadedFile` — sanitize, sandbox, write
- `resolve(uploadId): Path` — returns sandboxed path for a given uploadId
- `delete(uploadId)` — cleanup

## Component: WebProgressListener
- Implements `MigrationProgressListener`
- Constructor: `String jobId, SseEmitterRegistry registry`
- `onProgress(MigrationProgress)`: serializes to JSON, calls `registry.emit(jobId, event)`

## Component: SseEmitterRegistry
- `@Component`, `ConcurrentHashMap<String, SseEmitter>`
- `register(jobId): SseEmitter` — creates emitter with 5-min timeout
- `emit(jobId, event)` — sends SSE event; removes emitter on send error
- `complete(jobId)` — completes and removes emitter
- `error(jobId, message)` — sends error event, completes emitter

## Component: ZipPackager
- `@Component`
- `packageProject(Path projectDir): Path` — creates temp ZIP, returns path
- `stream(Path zipFile, HttpServletResponse)` — streams ZIP, deletes temp file after

## Component: CredentialStore
- `@Component`, `ConcurrentHashMap<String, char[]>`
- `store(configId, password): void`
- `retrieve(configId): char[]`
- `clear(configId): void` — zeroes and removes

## Component: SecurityConfig
- `@Configuration`, `@EnableWebSecurity`
- Configures: deny-by-default, public paths, HTTP security headers, CORS, session management
- Form login with `/login` page; logout clears session

## Component: GlobalExceptionHandler
- `@ControllerAdvice`
- Handles all exception types → appropriate HTTP status + generic JSON error body
- SECURITY-09: no internal details in responses

## Component: WebApplication (Spring Boot main)
- `@SpringBootApplication`
- `@EnableAsync` for async job execution
- `ThreadPoolTaskExecutor` bean configuration

## Thymeleaf Templates (src/main/resources/templates/)
- `layout.html` — shared layout fragment
- `index.html` — home page (upload + JDBC forms)
- `progress.html` — job progress view (SSE-connected)
- `report.html` — report view
- `login.html` — Spring Security login page

## Static Assets (src/main/resources/static/)
- `css/bootstrap.min.css` — Bootstrap 5 (local copy)
- `js/bootstrap.bundle.min.js` — Bootstrap JS (local copy)
- `js/progress.js` — SSE client logic
