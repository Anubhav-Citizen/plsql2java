# NFR Requirements — Unit 1: Discovery & Dependency Analysis

---

## Performance

- **NFR-U1-P1**: Schema discovery for a 500-object schema MUST complete within 5 minutes (JDBC and FILE modes)
- **NFR-U1-P2**: JDBC queries MUST use batch fetching where possible (fetch size: 100 rows) to minimize round-trips
- **NFR-U1-P3**: DDL file parsing MUST process files sequentially; parallel parsing is not required for MVP
- **NFR-U1-P4**: Dependency analysis (graph construction + cycle detection + topological sort) for 500 objects MUST complete within 30 seconds
- **NFR-U1-P5**: DiscoveryResult and DependencyGraph serialization to JSON MUST complete within 10 seconds for 500 objects

## Security

- **NFR-U1-S1**: JDBC password MUST be sourced from environment variables — never from plain-text config files (SECURITY-12)
- **NFR-U1-S2**: JDBC password MUST NOT appear in any log output at any level (SECURITY-03)
- **NFR-U1-S3**: DDL file paths MUST be validated to prevent path traversal (no `../` sequences) (SECURITY-05)
- **NFR-U1-S4**: DDL file content MUST be parsed only — never executed against any database (SECURITY-09)
- **NFR-U1-S5**: JDBC connections MUST use TLS if the Oracle server supports it; connection string MUST NOT disable SSL (SECURITY-01)
- **NFR-U1-S6**: All exceptions thrown to the delivery layer MUST contain user-friendly messages only — no stack traces, internal paths, or DB details (SECURITY-09, SECURITY-15)

## Reliability

- **NFR-U1-R1**: A single object parse failure MUST NOT stop the entire discovery operation (fail-partial strategy)
- **NFR-U1-R2**: JDBC connections MUST always be closed in finally blocks (no connection leaks) (SECURITY-15)
- **NFR-U1-R3**: If discovery is interrupted mid-run, partial results MUST be saved to the output directory
- **NFR-U1-R4**: All file I/O operations MUST have explicit error handling (SECURITY-15)

## Maintainability

- **NFR-U1-M1**: Oracle data dictionary query strings MUST be externalized as named constants (not inline SQL strings)
- **NFR-U1-M2**: DDL parsing regex patterns MUST be externalized as named constants with comments explaining each pattern
- **NFR-U1-M3**: Unit test coverage for Unit 1 MUST be ≥80%
- **NFR-U1-M4**: All public methods MUST have Javadoc comments

## Logging

- **NFR-U1-L1**: Structured logging MUST be used (SLF4J + Logback) with fields: timestamp, migrationId, level, component, message (SECURITY-03)
- **NFR-U1-L2**: Log levels: DEBUG for per-object progress, INFO for stage start/complete, WARN for non-fatal errors, ERROR for fatal errors
- **NFR-U1-L3**: No credentials, file contents, or PII in any log output (SECURITY-03)
