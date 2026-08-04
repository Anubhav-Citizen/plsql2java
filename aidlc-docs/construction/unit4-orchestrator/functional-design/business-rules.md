# Business Rules — Unit 4: Migration Orchestrator

---

## BR-OR01: Delivery Layer Isolation
- CLI and Web components MUST call MigrationOrchestratorService only — never core engine services directly

## BR-OR02: Fail-Partial, Not Fail-All
- A single object translation failure MUST NOT abort the pipeline
- Failed objects are logged at WARN and added to skippedObjects
- Pipeline continues for all remaining objects

## BR-OR03: Operation Mode Enforcement
- ANALYZE mode MUST only execute Discovery + Dependency Analysis
- GENERATE mode MUST execute the full pipeline (all 6 stages)
- REPORT mode MUST load persisted state and only execute Report Generation

## BR-OR04: Progress Events Required
- Every pipeline stage transition MUST emit a MigrationProgress event
- Per-object progress MUST be emitted during Translation (one event per object)
- ProgressEventBus listener exceptions MUST NOT propagate to the orchestrator

## BR-OR05: No Credentials in Progress Events
- MigrationProgress messages MUST NOT contain JDBC URLs, passwords, or schema credentials

## BR-OR06: Migration Order Respected
- GENERATE mode MUST process objects in the leaf-first migration order from DependencyGraph
- If DependencyGraph has no migration order, objects are processed in discovery order

## BR-OR07: Partial Result Status
- MigrationJobStatus MUST be set to PARTIAL (not COMPLETED) if any objects were skipped
- MigrationJobStatus MUST be set to FAILED only if the pipeline cannot produce any output

## BR-OR08: Output Directory Validation
- outputDir MUST be validated before pipeline start (writable, no path traversal)
- If outputDir does not exist, it MUST be created before writing

## BR-OR09: MDC Logging Context
- migrationId MUST be set in MDC at pipeline start and cleared in finally block
- Each object's name MUST be set in MDC during per-object processing
