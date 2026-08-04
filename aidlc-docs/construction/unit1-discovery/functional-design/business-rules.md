# Business Rules — Unit 1: Discovery & Dependency Analysis

---

## BR-01: JDBC Credential Security
- JDBC password MUST be sourced from an environment variable or a secrets-manager-backed config
- JDBC password MUST NOT be logged at any log level
- JDBC password MUST NOT be stored to disk in any format
- JDBC URL and username MAY be logged at DEBUG level (not password)

## BR-02: JDBC Connection Lifecycle
- A JDBC connection MUST be closed in a finally block (or try-with-resources) — always, even on error
- Connection timeout MUST be enforced (default: 30 seconds); configurable via MigrationConfig
- If connection fails, throw DiscoveryException with the JDBC error message (not a stack trace to the user)

## BR-03: Supported Object Types
- Only the following Oracle object types are discovered: PACKAGE, PACKAGE BODY, PROCEDURE, FUNCTION, TRIGGER, VIEW, SEQUENCE, TYPE, TYPE BODY
- All other object types (TABLE, INDEX, CONSTRAINT, SYNONYM, etc.) are silently ignored during discovery
- PACKAGE and PACKAGE BODY with the same name are linked into a single OracleObject (sourceSpec + sourceBody)

## BR-04: Objects with Compilation Errors
- Objects with compilation errors in the source schema MUST be included in DiscoveryResult (not silently skipped)
- Such objects MUST have `hasCompilationErrors = true`
- Such objects MUST be flagged in the analysis summary and migration report
- Translation of objects with compilation errors proceeds with best-effort; confidence score is capped at 50%

## BR-05: DDL File Validation
- Files with extensions other than `.sql` or `.ddl` MUST be rejected with a clear error message
- Files exceeding the maximum size limit (default: 50MB) MUST be rejected with a clear error message
- Malformed DDL statements within a file MUST be logged as DiscoveryErrors and skipped — processing continues for remaining statements in the file
- Empty files are accepted but produce zero OracleObjects (not an error)

## BR-06: DDL File Sandboxing
- DDL file content MUST be treated as data only — no execution of file content
- The DDL parser MUST operate in a sandboxed mode (no JDBC execution of parsed statements)
- File paths provided by users MUST be validated to prevent path traversal attacks (no `../` sequences)

## BR-07: Schema Name Handling
- In JDBC mode: if schemaName is not provided, use the authenticated user's schema (derived from JDBC username, uppercased)
- In FILE mode: schemaName is derived from the first `CREATE` statement's owner prefix, or set to "UNKNOWN" if not determinable
- Schema name comparison is always case-insensitive (Oracle stores names in uppercase)

## BR-08: Empty Schema Handling
- A schema with zero discoverable objects is valid — DiscoveryResult is returned with an empty objects list
- An empty schema MUST NOT cause an error; it produces an analysis summary stating "No supported objects found"

## BR-09: Dependency Reference Scope
- Only references to objects present in the current DiscoveryResult are recorded as DependencyEdges
- References to external schemas, Oracle built-in packages (DBMS_*, UTL_*, etc.), or unknown objects are ignored
- Self-references (an object referencing itself) are ignored

## BR-10: Circular Dependency Handling
- Circular dependencies MUST be detected and reported — they are NOT an error that stops processing
- Objects involved in circular dependencies are included in migrationOrder at the end, after all non-circular objects
- Each CircularDependency record includes the full cycle path for user review

## BR-11: Progress Reporting
- A ProgressEvent MUST be emitted after each object is discovered or analyzed
- ProgressEvent.percentComplete is calculated as: (processedCount / totalCount) * 100
- If totalCount is unknown at start (e.g., JDBC mode before full object list is fetched), percentComplete is set to -1 until totalCount is known

## BR-12: Partial Failure Handling
- A single object parse failure MUST NOT stop the entire discovery operation
- Failed objects are recorded as DiscoveryErrors and excluded from DiscoveryResult.objects
- If ALL objects fail, DiscoveryResult is returned with an empty objects list and all errors recorded
- Fatal errors (JDBC connection failure, output directory not writable) propagate immediately as DiscoveryException

## BR-13: Result Persistence
- DiscoveryResult and DependencyGraph MUST be persisted to the output directory after each successful operation
- Existing result files in the output directory are overwritten without warning
- If the output directory does not exist, it MUST be created automatically
- If the output directory is not writable, throw DiscoveryException with a clear message

## BR-14: Discovery Coverage Target
- The discovery implementation MUST achieve ≥95% coverage of all supported Oracle object types present in the schema
- Coverage is measured as: (objects successfully discovered / total objects of supported types in schema) * 100
