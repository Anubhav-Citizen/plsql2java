# Infrastructure Design — Unit 2: PL/SQL Translation Engine

---

## Infrastructure Summary

Unit 2 (`plsql2java-translation`) is a pure library module with no external infrastructure dependencies. It is consumed as a Maven dependency by Unit 4 (Orchestrator), which handles deployment.

## Compute

- **Runtime**: JVM (Java 17) — in-process within the host application (CLI JAR or Web container)
- **No dedicated compute resource** — runs as a library, not a standalone service

## Storage

- **None** — all processing is in-memory
- Grammar files and mapping tables are bundled in the JAR (classpath resources)
- TranslationResults are passed in-memory to Unit 3 (Code Generator)

## Networking

- **None** — no network calls, no ports, no HTTP endpoints

## Security Controls

| Control | Implementation |
|---|---|
| Input treated as data only | ANTLR4 parses PL/SQL as text — no execution (SECURITY-09) |
| No credentials in output | BR-T14 enforced in rule implementations (SECURITY-03) |
| Safe error messages | Generic messages to callers, details logged only (SECURITY-15) |
| Resource cleanup | Try-with-resources for all classpath I/O (SECURITY-15) |

## Build Artifact

- **Output**: `plsql2java-translation-{version}.jar` (Maven module artifact)
- **Consumed by**: `plsql2java-orchestrator` (Unit 4) as a `<dependency>`
- **ANTLR4 grammar compilation**: Handled by `antlr4-maven-plugin` during `generate-sources` phase
