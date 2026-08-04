# Infrastructure Design — Unit 1: Discovery & Dependency Analysis

---

## Infrastructure Overview

Unit 1 (`plsql2java-discovery`) is a **pure Java library module** with no dedicated infrastructure of its own. It has two external runtime dependencies:

1. **Oracle Database** (external, user-provided) — accessed via JDBC for live discovery mode
2. **Local File System** — for DDL file input and result persistence (output directory)

No cloud services, message queues, caches, or databases are required for Unit 1 itself.

---

## External Dependency: Oracle Database (JDBC Mode)

| Attribute | Detail |
|---|---|
| Type | External Oracle Database (user-provided) |
| Access Method | JDBC (Oracle JDBC Driver ojdbc11) |
| Protocol | TCP/IP with optional TLS |
| Authentication | Username + password (from environment variable) |
| Required Permissions | SELECT on ALL_OBJECTS, ALL_SOURCE, ALL_VIEWS, ALL_SEQUENCES, ALL_ERRORS, ALL_TYPES for the target schema |
| Connection Pooling | Not required — single connection per discovery run, closed after completion |

**Security Requirements**:
- TLS MUST be used if the Oracle server supports it
- Password sourced from `ORACLE_PASSWORD` environment variable (or configurable env var name)
- Connection closed immediately after discovery completes

---

## Local File System Usage

| Usage | Path | Access |
|---|---|---|
| DDL input files | User-provided paths | Read-only |
| Discovery result | `{outputDir}/discovery-result.json` | Write |
| Dependency graph | `{outputDir}/dependency-graph.json` | Write |
| Analysis summary | `{outputDir}/analysis-summary.md` | Write |

**Security Requirements**:
- Input file paths validated for path traversal before access
- Output directory created if it doesn't exist
- No execution of file content

---

## Maven Module Packaging

Unit 1 is packaged as a standard Maven JAR (`plsql2java-discovery-{version}.jar`). It is consumed as a compile-time dependency by:
- `plsql2java-orchestrator` (Unit 4)
- `plsql2java-translation` (Unit 2) — for shared domain models only

No standalone executable is produced by Unit 1.
