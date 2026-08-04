# Business Logic Model — Unit 5: CLI Delivery

## Overview
Unit 5 is the CLI delivery layer. It translates command-line arguments and YAML config into `MigrationConfig`, delegates to `MigrationOrchestratorService`, and renders results to stdout with structured progress output and appropriate exit codes.

---

## Command Hierarchy

```
plsql2java
├── analyze    — Story 7.1: Discover PL/SQL objects and analyze dependencies
├── generate   — Story 7.2: Full pipeline (analyze + translate + codegen + report)
└── report     — Story 7.3: Regenerate report from existing migration output
```

---

## Story 7.1 — Analyze Command

**Trigger**: `plsql2java analyze [options]`

**Algorithm**:
1. Parse CLI flags into `CliConfig`
2. Load YAML config file if `--config` provided; merge with CLI flags (CLI flags override)
3. Validate: output directory writable, at least one source (JDBC or DDL files) provided
4. Map `CliConfig` → `MigrationConfig` with `operationMode = ANALYZE`
5. Register `CliProgressListener` on `ProgressEventBus`
6. Call `orchestrator.analyze(config)`
7. Print `AnalysisResult` summary: object count by type, dependency count, migration order
8. Exit with `ExitCode.SUCCESS`

**Output format**:
```
[INFO] Connecting to Oracle...
[INFO] Discovered 42 objects (15 procedures, 12 functions, 8 packages, 7 tables)
[INFO] Dependency graph: 38 edges, 0 circular dependencies
[INFO] Migration order: 42 objects in topological sequence
[INFO] Analysis complete. Results written to: /output/analysis-result.json
```

---

## Story 7.2 — Generate Command

**Trigger**: `plsql2java generate [options]`

**Algorithm**:
1. Parse CLI flags into `CliConfig`
2. Load and merge YAML config (CLI flags override)
3. Validate: output directory writable, target package valid Java identifier, source provided
4. Map `CliConfig` → `MigrationConfig` with `operationMode = GENERATE`
5. Register `CliProgressListener` on `ProgressEventBus`
6. Call `orchestrator.generate(config)`
7. Print `MigrationResult` summary: translated count, skipped count, confidence scores, output path
8. If `result.isPartial()`: exit `ExitCode.PARTIAL_SUCCESS`; else `ExitCode.SUCCESS`

**Output format**:
```
[INFO] Starting migration pipeline...
[PROGRESS] Discovery: 42 objects found
[PROGRESS] Translation: 40/42 translated (2 skipped)
[PROGRESS] Code generation: complete
[PROGRESS] Confidence scoring: avg 0.87
[PROGRESS] Report generation: complete
[WARN] 2 objects skipped: PROC_LEGACY_CURSOR, PKG_UNSUPPORTED_BULK
[INFO] Migration complete. Output: /output/generated-project/
[INFO] Report: /output/migration-report.html
```

---

## Story 7.3 — Report Command

**Trigger**: `plsql2java report [options]`

**Algorithm**:
1. Parse CLI flags: `--output-dir` (required), `--format` (md|html|both, default=both)
2. Validate: output directory exists and contains prior migration results
3. Map to `MigrationConfig` with `operationMode = REPORT`
4. Call `orchestrator.report(config)`
5. Print report paths
6. Exit `ExitCode.SUCCESS`

---

## Config Loading Logic (ConfigLoader)

**Priority order** (highest to lowest):
1. CLI flags (explicit user input)
2. Environment variables (`PLSQL2JAVA_JDBC_URL`, `PLSQL2JAVA_JDBC_PASSWORD`, etc.)
3. YAML config file (`--config migration-config.yml`)
4. Defaults (confidence threshold = 0.7, output dir = `./plsql2java-output`)

**YAML config structure** (migration-config-template.yml):
```yaml
oracle:
  jdbcUrl: jdbc:oracle:thin:@localhost:1521:XE
  username: scott
  # password: use PLSQL2JAVA_JDBC_PASSWORD env var
  ddlFiles: []

output:
  directory: ./plsql2java-output
  targetPackage: com.example.migrated

migration:
  confidenceThreshold: 0.7
  objectTypes: []   # empty = all types
```

---

## Progress Output Logic (CliProgressListener)

- Implements `MigrationProgressListener`
- On each `MigrationProgress` event:
  - If `verbose=true`: print `[PROGRESS] stage: message (pct%)`
  - If `verbose=false`: print only stage transitions (not per-object events)
- Errors always printed regardless of verbose flag
- Uses `System.out` (not logger) for user-facing output
- Uses SLF4J logger for internal diagnostic logging

---

## Error Handling

| Error Type | Behavior |
|---|---|
| Missing required arg | Print usage help + exit `VALIDATION_ERROR(2)` |
| Invalid YAML config | Print parse error location + exit `VALIDATION_ERROR(2)` |
| JDBC connection failure | Print connection error + exit `EXECUTION_ERROR(3)` |
| Output dir not writable | Print path + exit `IO_ERROR(4)` |
| Partial translation failure | Print skipped objects + exit `PARTIAL_SUCCESS(1)` |
| Unexpected exception | Print generic message + exit `EXECUTION_ERROR(3)` |
